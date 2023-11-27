resource "aws_apprunner_service" "service" {
  service_name = var.service_name

  instance_configuration {
    cpu = "0.25 vCPU"
    instance_role_arn = aws_iam_role.role_for_apprunner_service.arn
    memory = "1 GB"
  }

  source_configuration {
    authentication_configuration {
      access_role_arn = "arn:aws:iam::244530008913:role/kan-2041-iam-role"
    }
    image_repository {
      image_configuration {
        port = var.port
      }
      image_identifier      = var.image
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
    effect    = var.allow
    actions   = ["rekognition:*"]
    resources = ["*"]
  }
  
  statement  {
    effect    = var.allow
    actions   = ["s3:*"]
    resources = ["*"]
  }

  statement  {
    effect    = var.allow
    actions   = ["cloudwatch:*"]
    resources = ["*"]
  }

  statement  {
    effect    = var.allow
    actions   = ["apprunner:*"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "policy" {
  name        = var.aws_iam_policy
  description = "Policy for apprunner"
  policy      = data.aws_iam_policy_document.policy.json
}


resource "aws_iam_role_policy_attachment" "attachment" {
  role       = aws_iam_role.role_for_apprunner_service.name
  policy_arn = aws_iam_policy.policy.arn
}