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
            "total_scans.value"
          ],
          [
            "${var.dashboard_name}",
            "non_violations.value"
          ],
          [
            "${var.dashboard_name}",
            "violations.value"
          ]
        ],
        "period": 60,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "Total PPE Scans"
      }
    },
    {
      "type": "metric",
      "x": 12,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.dashboard_name}",
            "total_full_scans.value"
          ],
          [
            "${var.dashboard_name}",
            "non_violations_all_regions.value"
          ],
          [
            "${var.dashboard_name}",
            "violations_all_regions.value"
          ]
        ],
        "period": 60,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "Total PPE Scans for all regions"
      }
    }
  ]
}
DASHBOARD
}