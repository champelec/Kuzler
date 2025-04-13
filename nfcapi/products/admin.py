from django.contrib import admin

from django.contrib import admin
from .models import Product

@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = ('nfc_id', 'name', 'price', 'created_at')
    search_fields = ('nfc_id', 'name')
    list_filter = ('created_at',)