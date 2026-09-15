# Generated manually for the Spring Boot statistics synchronization feature.

from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [
        ("analytics", "0001_initial"),
    ]

    operations = [
        migrations.AddConstraint(
            model_name="servicelog",
            constraint=models.UniqueConstraint(
                fields=("date", "category"),
                name="unique_service_log_date_category",
            ),
        ),
    ]
