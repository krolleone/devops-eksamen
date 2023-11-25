resource "aws_apprunner_service" "service" {
  service_name = var.service_name

  instance_configuration {
    cpu = var.cpu
    instance_role_arn = aws_iam_role.role_for_apprunner_service.arn
    memory = var.memory
  }

  source_configuration {
    authentication_configuration {
      access_role_arn = "arn:aws:iam::244530008913:role/service-role/AppRunnerECRAccessRole"
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

resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.dashboard_name
  dashboard_body = <<DASHBOARD
{
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.dashboard_name}",
            "account_count.value"
          ]
        ],
        "period": 300,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "Total number of accounts"
      }
    }
  ]
}
DASHBOARD
}

/*
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.dashboard_name
  dashboard_body = <<DASHBOARD
{
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.total_face_ppe_scans}",
            "total_scans.value"
          ]
        ],
        "period": 60,
        "stat": "Maximum",
        "region": "${var.region}",
        "title": "Total PPE Scans"
      }
    }
  ]
}
DASHBOARD
}
*/
