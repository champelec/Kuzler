from django.db import models

class Product(models.Model):
    nfc_id = models.CharField(max_length=100, unique=False)
    name = models.CharField(max_length=255)
    price = models.DecimalField(max_digits=10, decimal_places=2)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.name} (NFC: {self.nfc_id})"
