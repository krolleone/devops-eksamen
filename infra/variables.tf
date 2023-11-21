variable "prefix" {
  type = string
  default = "kan-2041-ecr"
}

variable "service_name" {
  type = string
  default = "kan-2041"
}

variable "port" {
  type = string
  default = "8080"
}

variable "region" {
  type = string
  default = "eu-west-1"
}

variable "aws_iam_role" {
  type = string
  default = "kan-2041-iam-role"
}

variable "aws_iam_policy" {
  type = string
  default = "kan-2041-iam-policy"
}

variable "image" {
  type = string
  default = "244530008913.dkr.ecr.eu-west-1.amazonaws.com/kan-2041-ecr:latest"
}

variable "data_policy_statement_effect_allow" {
  type = string
  default = "Allow"
}

variable "cpu" {
  type = string
  default = "1 vCPU"
}

variable "memory" {
  type = string
  default = "2 GB"
}

variable "dashboard_name" {
  type = string
  default = "kan-2041-dashboard"
}

variable "total_face_ppe_scans" {
  type = string
  default = "Total scans of Face-violations"
}
