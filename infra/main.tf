resource "aws_apprunner_service" "service" {
  service_name = var.service_name

  instance_configuration {
    cpu = "256"
    instance_role_arn = aws_iam_role.role_for_apprunner_service.arn
    memory = "1024"
  }

  source_configuration {
    authentication_configuration {
      access_role_arn = "arn:aws:iam::244530008913:role/service-role/AppRunnerECRAccessRole"
    }
    image_repository {
      image_configuration {
        port = var.port
      }
      image_identifier      = var.image_repository_image_identifier
      image_repository_type = "ECR"
    }
    auto_deployments_enabled = true
  }
}

resource "aws_iam_role" "role_for_apprunner_service" {
  name               = var.aws_iam_role
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}


data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["tasks.apprunner.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "policy" {
  statement {
    effect    = var.data_policy_statement_effect_allow
    actions   = ["rekognition:*"]
    resources = ["*"]
  }
  
  statement  {
    effect    = var.data_policy_statement_effect_allow
    actions   = ["s3:*"]
    resources = ["*"]
  }

  statement  {
    effect    = var.data_policy_statement_effect_allow
    actions   = ["cloudwatch:*"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "policy" {
  name        = var.aws_iam_policy
  description = "Policy for apprunner instance I think"
  policy      = data.aws_iam_policy_document.policy.json
}


resource "aws_iam_role_policy_attachment" "attachment" {
  role       = aws_iam_role.role_for_apprunner_service.name
  policy_arn = aws_iam_policy.policy.arn
}

