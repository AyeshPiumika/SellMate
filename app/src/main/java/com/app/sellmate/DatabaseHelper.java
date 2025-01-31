package com.app.sellmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sellmate.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_SALESMEN = "salesmen";
    private static final String TABLE_ITEMS = "items";
    private static final String TABLE_INVOICES = "invoices";
    private static final String TABLE_INVOICE_ITEMS = "invoice_items";

    // Common columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONTACT_NUMBER = "contact_number";
    private static final String COLUMN_CONTACT_PERSON_NAME = "contact_person_name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_ITEM_ID = "id";
    private static final String COLUMN_ITEM_NAME = "name";

    // Customers table columns
    private static final String COLUMN_CUSTOMER_ID = COLUMN_ID;
    private static final String COLUMN_CUSTOMER_NAME = COLUMN_NAME;
    private static final String COLUMN_CUSTOMER_CONTACT_NUMBER = COLUMN_CONTACT_NUMBER;
    private static final String COLUMN_CUSTOMER_CONTACT_PERSON_NAME = COLUMN_CONTACT_PERSON_NAME;
    private static final String COLUMN_CUSTOMER_ADDRESS = COLUMN_ADDRESS;
    private static final String COLUMN_CUSTOMER_EXISTING_ID = "existingid";

    // Salesmen table columns
    private static final String COLUMN_SALESMAN_ID = COLUMN_ID;
    private static final String COLUMN_SALESMAN_NAME = COLUMN_NAME;
    private static final String COLUMN_SALESMAN_CONTACT_NUMBER = COLUMN_CONTACT_NUMBER;
    private static final String COLUMN_SALESMAN_NIC_NUMBER = "nic_number";

    // Items table columns
    private static final String COLUMN_ITEM_SELLING_PRICE = "sellingPrice";
    private static final String COLUMN_ITEM_EXISTING_ID = "existingid";

    // Invoices table columns
    private static final String COLUMN_INVOICE_ID = COLUMN_ID;
    private static final String COLUMN_INVOICE_NUMBER = "invoice_number";
    private static final String COLUMN_INVOICE_DATE = "invoice_date";
    private static final String COLUMN_CUSTOMER_ID_FK = "customer_id";
    private static final String COLUMN_SALESMAN_ID_FK = "salesman_id";
    private static final String COLUMN_TOTAL_AMOUNT = "total_amount";
    private static final String COLUMN_DISCOUNT = "discount";

    // Invoice Items table columns
    private static final String COLUMN_INVOICE_ITEM_ID = COLUMN_ID;
    private static final String COLUMN_INVOICE_ID_FK = "invoice_id";
    private static final String COLUMN_ITEM_ID_FK = "item_id";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_ITEM_TOTAL = "item_total";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CUSTOMERS_TABLE = "CREATE TABLE " + TABLE_CUSTOMERS + "("
                + COLUMN_CUSTOMER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CUSTOMER_NAME + " TEXT,"
                + COLUMN_CUSTOMER_CONTACT_NUMBER + " TEXT,"
                + COLUMN_CUSTOMER_CONTACT_PERSON_NAME + " TEXT,"
                + COLUMN_CUSTOMER_ADDRESS + " TEXT,"
                + COLUMN_CUSTOMER_EXISTING_ID + " TEXT UNIQUE" + ")";
        db.execSQL(CREATE_CUSTOMERS_TABLE);

        String CREATE_SALESMEN_TABLE = "CREATE TABLE " + TABLE_SALESMEN + "("
                + COLUMN_SALESMAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SALESMAN_NAME + " TEXT,"
                + COLUMN_SALESMAN_CONTACT_NUMBER + " TEXT,"
                + COLUMN_SALESMAN_NIC_NUMBER + " TEXT" + ")";
        db.execSQL(CREATE_SALESMEN_TABLE);

        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                + COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ITEM_NAME + " TEXT,"
                + COLUMN_ITEM_SELLING_PRICE + " REAL,"
                + COLUMN_ITEM_EXISTING_ID + " TEXT UNIQUE" + ")";
        db.execSQL(CREATE_ITEMS_TABLE);

        String CREATE_INVOICES_TABLE = "CREATE TABLE " + TABLE_INVOICES + "("
                + COLUMN_INVOICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_INVOICE_NUMBER + " TEXT,"
                + COLUMN_INVOICE_DATE + " TEXT,"
                + COLUMN_CUSTOMER_ID_FK + " TEXT,"
                + COLUMN_SALESMAN_ID_FK + " TEXT,"
                + COLUMN_TOTAL_AMOUNT + " REAL,"
                + COLUMN_DISCOUNT + " REAL,"
                + "FOREIGN KEY(" + COLUMN_CUSTOMER_ID_FK + ") REFERENCES " + TABLE_CUSTOMERS + "(" + COLUMN_CUSTOMER_EXISTING_ID + "),"
                + "FOREIGN KEY(" + COLUMN_SALESMAN_ID_FK + ") REFERENCES " + TABLE_SALESMEN + "(" + COLUMN_SALESMAN_ID + "))";
        db.execSQL(CREATE_INVOICES_TABLE);

        String CREATE_INVOICE_ITEMS_TABLE = "CREATE TABLE " + TABLE_INVOICE_ITEMS + "("
                + COLUMN_INVOICE_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_INVOICE_ID_FK + " TEXT,"
                + COLUMN_ITEM_ID_FK + " TEXT,"
                + COLUMN_QUANTITY + " INTEGER,"
                + COLUMN_ITEM_TOTAL + " REAL,"
                + "FOREIGN KEY(" + COLUMN_INVOICE_ID_FK + ") REFERENCES " + TABLE_INVOICES + "(" + COLUMN_INVOICE_ID + "),"
                + "FOREIGN KEY(" + COLUMN_ITEM_ID_FK + ") REFERENCES " + TABLE_ITEMS + "(" + COLUMN_ITEM_EXISTING_ID + "))";
        db.execSQL(CREATE_INVOICE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALESMEN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVOICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVOICE_ITEMS);
        onCreate(db);
    }

    // Customer methods
    public void addCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, customer.getName());
        values.put(COLUMN_CUSTOMER_CONTACT_NUMBER, customer.getContactNumber());
        values.put(COLUMN_CUSTOMER_CONTACT_PERSON_NAME, customer.getContactPersonName());
        values.put(COLUMN_CUSTOMER_ADDRESS, customer.getAddress());
        values.put(COLUMN_CUSTOMER_EXISTING_ID, customer.getExistingid());
        db.insert(TABLE_CUSTOMERS, null, values);
        db.close();
    }

    public void updateCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, customer.getName());
        values.put(COLUMN_CUSTOMER_CONTACT_NUMBER, customer.getContactNumber());
        values.put(COLUMN_CUSTOMER_CONTACT_PERSON_NAME, customer.getContactPersonName());
        values.put(COLUMN_CUSTOMER_ADDRESS, customer.getAddress());
        values.put(COLUMN_CUSTOMER_EXISTING_ID, customer.getExistingid());
        db.update(TABLE_CUSTOMERS, values, COLUMN_CUSTOMER_ID + " = ?", new String[]{String.valueOf(customer.getId())});
        db.close();
    }

    public void deleteCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CUSTOMERS, COLUMN_CUSTOMER_ID + " = ?", new String[]{String.valueOf(customer.getId())});
        db.close();
    }

    public ArrayList<Customer> getAllCustomers() {
        ArrayList<Customer> customers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CUSTOMERS, null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME));
                String contactNumber = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_CONTACT_NUMBER));
                String contactPersonName = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_CONTACT_PERSON_NAME));
                String address = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_ADDRESS));
                String existingid = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_EXISTING_ID));
                customers.add(new Customer(id, name, contactNumber, contactPersonName, address, existingid));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return customers;
    }

    // Salesman methods
    public void addSalesman(Salesmen salesman) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SALESMAN_NAME, salesman.getName());
        values.put(COLUMN_SALESMAN_CONTACT_NUMBER, salesman.getContactNumber());
        values.put(COLUMN_SALESMAN_NIC_NUMBER, salesman.getNicNumber());
        db.insert(TABLE_SALESMEN, null, values);
        db.close();
    }

    public void updateSalesman(Salesmen salesman) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SALESMAN_NAME, salesman.getName());
        values.put(COLUMN_SALESMAN_CONTACT_NUMBER, salesman.getContactNumber());
        values.put(COLUMN_SALESMAN_NIC_NUMBER, salesman.getNicNumber());
        db.update(TABLE_SALESMEN, values, COLUMN_SALESMAN_ID + " = ?", new String[]{String.valueOf(salesman.getId())});
        db.close();
    }

    public void deleteSalesman(Salesmen salesman) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SALESMEN, COLUMN_SALESMAN_ID + " = ?", new String[]{String.valueOf(salesman.getId())});
        db.close();
    }

    public ArrayList<Salesmen> getAllSalesmen() {
        ArrayList<Salesmen> salesmen = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SALESMEN, null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_NAME));
                String contactNumber = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_CONTACT_NUMBER));
                String nicNumber = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_NIC_NUMBER));
                salesmen.add(new Salesmen(id, name, contactNumber, nicNumber));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return salesmen;
    }

    // Item methods
    public void addItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, item.getName());
        values.put(COLUMN_ITEM_SELLING_PRICE, item.getSellingPrice());
        values.put(COLUMN_ITEM_EXISTING_ID, item.getExistingid());
        db.insert(TABLE_ITEMS, null, values);
        db.close();
    }

    public void updateItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, item.getName());
        values.put(COLUMN_ITEM_SELLING_PRICE, item.getSellingPrice());
        values.put(COLUMN_ITEM_EXISTING_ID, item.getExistingid());
        db.update(TABLE_ITEMS, values, COLUMN_ITEM_ID + " = ?", new String[]{String.valueOf(item.getId())});
        db.close();
    }

    public void deleteItem(String itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, COLUMN_ITEM_ID + " = ?", new String[]{itemId});
        db.close();
    }

    public ArrayList<Item> getAllItems() {
        ArrayList<Item> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_NAME));
                double sellingPrice = cursor.getDouble(cursor.getColumnIndex(COLUMN_ITEM_SELLING_PRICE));
                String existingid = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_EXISTING_ID));
                items.add(new Item(id, name, sellingPrice, existingid));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    // Invoice methods
    public long addInvoice(Invoice invoice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INVOICE_NUMBER, invoice.getInvoiceNumber());
        values.put(COLUMN_INVOICE_DATE, invoice.getInvoiceDate());
        values.put(COLUMN_CUSTOMER_ID_FK, invoice.getCustomerId());
        values.put(COLUMN_SALESMAN_ID_FK, invoice.getSalesmanId());
        values.put(COLUMN_TOTAL_AMOUNT, invoice.getTotalAmount());
        values.put(COLUMN_DISCOUNT, invoice.getDiscount());
        // Insert the new row and get the row ID
        long id = db.insert(TABLE_INVOICES, null, values);
        db.close();
        return id;
    }

    public void addInvoiceItem(InvoiceItem invoiceItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INVOICE_ID_FK, invoiceItem.getInvoiceId());
        values.put(COLUMN_ITEM_ID_FK, invoiceItem.getItemId());
        values.put(COLUMN_QUANTITY, invoiceItem.getQuantity());
        values.put(COLUMN_ITEM_TOTAL, invoiceItem.getItemTotal());
        db.insert(TABLE_INVOICE_ITEMS, null, values);
        db.close();
    }

//    public ArrayList<Invoice> getInvoicesForToday() {
//        ArrayList<Invoice> invoices = new ArrayList<>();
//        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INVOICES + " WHERE " + COLUMN_INVOICE_DATE + " = ?", new String[]{todayDate});
//        if (cursor.moveToFirst()) {
//            do {
//                String id = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_ID));
//                String invoiceNumber = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_NUMBER));
//                String date = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_DATE));
//                String customerId = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_ID_FK));
//                String salesmanId = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_ID_FK));
//                double totalAmount = cursor.getDouble(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT));
//                double discount = cursor.getDouble(cursor.getColumnIndex(COLUMN_DISCOUNT));
//                invoices.add(new Invoice(id, invoiceNumber, date, customerId, salesmanId, totalAmount, discount));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return invoices;
//    }

    public ArrayList<Invoice> getInvoicesForLastMonth() {
        ArrayList<Invoice> invoices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get current date and date one month ago
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        // Use Calendar to get the date one month ago
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        String oneMonthAgoDate = sdf.format(calendar.getTime());

        // Query to fetch invoices between one month ago and today
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INVOICES + " WHERE " + COLUMN_INVOICE_DATE + " BETWEEN ? AND ?", new String[]{oneMonthAgoDate, currentDate});

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_ID));
                String invoiceNumber = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_NUMBER));
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_DATE));
                String customerId = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_ID_FK));
                String salesmanId = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_ID_FK));
                double totalAmount = cursor.getDouble(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT));
                double discount = cursor.getDouble(cursor.getColumnIndex(COLUMN_DISCOUNT));
                invoices.add(new Invoice(id, invoiceNumber, date, customerId, salesmanId, totalAmount, discount));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return invoices;
    }

    public ArrayList<InvoiceItem> getInvoiceItems(String invoiceId) {
        ArrayList<InvoiceItem> invoiceItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INVOICE_ITEMS + " WHERE " + COLUMN_INVOICE_ID_FK + " = ?", new String[]{invoiceId});
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_ITEM_ID));
                String itemId = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_ID_FK));
                int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                double itemTotal = cursor.getDouble(cursor.getColumnIndex(COLUMN_ITEM_TOTAL));
                invoiceItems.add(new InvoiceItem(id, invoiceId, itemId, quantity, itemTotal));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return invoiceItems;
    }

    public Item getItemByName(String itemName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("items", null, "name = ?", new String[]{itemName}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            double sellingPrice = cursor.getDouble(cursor.getColumnIndex("sellingPrice"));
            String existingid = cursor.getString(cursor.getColumnIndex("existingid"));
            cursor.close();
            return new Item(id, name, sellingPrice, existingid);
        }
        return null;
    }

    public ArrayList<Invoice> getAllInvoices() {
        ArrayList<Invoice> invoices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Invoices", null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String invoiceNumber = cursor.getString(cursor.getColumnIndex("invoiceNumber"));
                String invoiceDate = cursor.getString(cursor.getColumnIndex("invoiceDate"));
                String customerId = cursor.getString(cursor.getColumnIndex("customerId"));
                String salesmanId = cursor.getString(cursor.getColumnIndex("salesmanId"));
                double totalAmount = cursor.getDouble(cursor.getColumnIndex("totalAmount"));
                double discount = cursor.getDouble(cursor.getColumnIndex("discount"));

                Invoice invoice = new Invoice(id, invoiceNumber, invoiceDate, customerId, salesmanId, totalAmount, discount);
                invoices.add(invoice);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return invoices;
    }

    // Salesman Login
    public Salesmen getSalesmenByCredentials(String contactNumber, String nicNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Salesmen salesman = null;
        Cursor cursor = db.query(TABLE_SALESMEN,
                null,
                COLUMN_SALESMAN_CONTACT_NUMBER + "=? AND " + COLUMN_SALESMAN_NIC_NUMBER + "=?",
                new String[]{contactNumber, nicNumber},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_ID));
            String name = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_NAME));
            salesman = new Salesmen(id, name, contactNumber, nicNumber);
            cursor.close();
        }
        db.close();
        return salesman;
    }

    // Get Salesman by ID
    public Salesmen getSalesmenById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Salesmen salesman = null;
        Cursor cursor = db.query(TABLE_SALESMEN,
                null,
                COLUMN_SALESMAN_ID + "=?",
                new String[]{id},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_NAME));
            String contactNumber = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_CONTACT_NUMBER));
            String nicNumber = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_NIC_NUMBER));
            salesman = new Salesmen(id, name, contactNumber, nicNumber);
            cursor.close();
        }
        db.close();
        return salesman;
    }

    public String getCustomerNameById(String customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String customerName = null;

        try {
            cursor = db.rawQuery("SELECT " + COLUMN_CUSTOMER_NAME + " FROM " + TABLE_CUSTOMERS + " WHERE " + COLUMN_CUSTOMER_ID + " = ?", new String[]{customerId});
            if (cursor != null && cursor.moveToFirst()) {
                customerName = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving customer name for ID " + customerId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d("DatabaseHelper", "Customer ID: " + customerId + ", Customer Name: " + (customerName != null ? customerName : "Not Found"));
        return customerName;
    }

    public Customer getCustomerById(String customerName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Customer customer = null;

        // Use COLUMN_CUSTOMER_EXISTING_ID instead of COLUMN_CUSTOMER_ID
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, COLUMN_CUSTOMER_NAME + " = ?", new String[]{customerName}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME));
            String contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_CONTACT_NUMBER));
            String contactPersonName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_CONTACT_PERSON_NAME));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_ADDRESS));
            String existingid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_EXISTING_ID));

            customer = new Customer(id, name, contactNumber, contactPersonName, address, existingid);
            cursor.close();
        } else {
            Log.e("DatabaseHelper", "Customer not found for Name: " + customerName);
        }

        db.close();
        return customer;
    }

    public Item getItemById(String itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Item item = null;
        Cursor cursor = db.query("items", null, "id = ?", new String[]{itemId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            double sellingPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("sellingPrice"));
            String existingid = cursor.getString(cursor.getColumnIndexOrThrow("existingid"));

            // Use the correct constructor
            item = new Item(id, name, sellingPrice, existingid);
            cursor.close();
        }

        return item;
    }

    public ArrayList<Invoice> getInvoicesByDateRange(String startDate, String endDate) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch invoices between the specified dates
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INVOICES + " WHERE " + COLUMN_INVOICE_DATE + " BETWEEN ? AND ?", new String[]{startDate, endDate});

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_ID));
                String invoiceNumber = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_NUMBER));
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_INVOICE_DATE));
                String customerId = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_ID_FK));
                String salesmanId = cursor.getString(cursor.getColumnIndex(COLUMN_SALESMAN_ID_FK));
                double totalAmount = cursor.getDouble(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT));
                double discount = cursor.getDouble(cursor.getColumnIndex(COLUMN_DISCOUNT));
                invoices.add(new Invoice(id, invoiceNumber, date, customerId, salesmanId, totalAmount, discount));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return invoices;
    }

}
