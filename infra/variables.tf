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

// Could make another variable for disallow, but opted out since it wont be used
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