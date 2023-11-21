terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.39.0"
    }
  }
  backend "s3" {
    bucket = "kan-2041-state-bucket"
    key    = "kan-2041/apprunner-actions.state"
    region = var.region
  }
}