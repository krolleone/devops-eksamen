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
  default = "kjell-role-thingy"
}

variable "aws_iam_policy" {
  type = string
  default = "kjell-apr-policy-thingy"
}

variable "image_repository_image_identifier" {
  type = string
  default = "244530008913.dkr.ecr.eu-west-1.amazonaws.com/kjell:latest"
}

// Could make another variable for disallow, but opted out since it wont be used
variable "data_policy_statement_effect_allow" {
  type = string
  default = "Allow"
}