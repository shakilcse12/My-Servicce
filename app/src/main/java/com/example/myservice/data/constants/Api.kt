package com.example.myservice.data.constants

object Api {
    const val BASE_URL = "http://192.168.1.102:8000/"
    const val BASE_URL3 = "http://192.168.18.10:8000/"
    const val BASE_URL2 = "https://deeptechcaresolution.com/"
    const val BASE_URL_AVD = "http://10.0.2.2:8000/"
    // Change this to your local server IP
    //const val LOGIN_ENDPOINT = "login"
    object Endpoints {
        const val LOGIN = "api/login"
        const val INVOICES = "api/sales"
        const val INVOICE_BY_SR = "api/sr/party-wise-collection"
        const val INVOICE_DETAILS = "api/sales/{invoiceId}"
        const val INVOICE_CREATE = "api/sales/create"
        const val INVOICE_COLLECT = "api/collection/create"
        const val PRODUCTS = "api/products"
        const val PARTIES = "api/parties"
        const val PARTIY_CREATE = "api/party/create"
        const val SR = "api/all-user-info"
    }
}