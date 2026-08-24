package com.example.util

object Localization {

    enum class Language(val code: String, val displayName: String, val nativeName: String) {
        MARATHI("mr", "Marathi", "मराठी"),
        HINDI("hi", "Hindi", "हिंदी"),
        ENGLISH("en", "English", "English");

        companion object {
            fun fromCode(code: String): Language {
                return entries.find { it.code.equals(code, ignoreCase = true) } ?: MARATHI
            }
        }
    }

    private val translations = mapOf(
        "app_name" to mapOf(
            "en" to "Sumit Attendance System",
            "hi" to "सुमित उपस्थिति प्रणाली",
            "mr" to "सुमित हजेरी प्रणाली"
        ),
        "department_title" to mapOf(
            "en" to "Sanitation Department Management",
            "hi" to "स्वच्छता विभाग प्रबंधन",
            "mr" to "स्वच्छता विभाग व्यवस्थापन"
        ),
        "admin_login" to mapOf(
            "en" to "Admin Login",
            "hi" to "व्यवस्थापक लॉगिन",
            "mr" to "अ‍ॅडमिन लॉगिन"
        ),
        "enter_mobile" to mapOf(
            "en" to "Enter Mobile Number",
            "hi" to "मोबाइल नंबर दर्ज करें",
            "mr" to "मोबाईल नंबर टाका"
        ),
        "get_otp" to mapOf(
            "en" to "Get OTP",
            "hi" to "ओटीपी प्राप्त करें",
            "mr" to "ओटीपी मिळवा"
        ),
        "enter_otp" to mapOf(
            "en" to "Enter 6-Digit OTP",
            "hi" to "6-अंकीय ओटीपी दर्ज करें",
            "mr" to "६-अंकी ओटीपी टाका"
        ),
        "verify_login" to mapOf(
            "en" to "Verify & Login",
            "hi" to "सत्यापित करें और लॉगिन करें",
            "mr" to "सत्यापित करा व लॉगिन करा"
        ),
        "attendance" to mapOf(
            "en" to "Attendance",
            "hi" to "उपस्थिति",
            "mr" to "हजेरी"
        ),
        "daily_attendance_pdf" to mapOf(
            "en" to "Daily Attendance PDF",
            "hi" to "दैनिक उपस्थिति पीडीएफ",
            "mr" to "दैनंदिन हजेरी अहवाल (PDF)"
        ),
        "workers" to mapOf(
            "en" to "Workers",
            "hi" to "कर्मचारी",
            "mr" to "कामगार"
        ),
        "settings_and_reports" to mapOf(
            "en" to "Settings & Reports",
            "hi" to "सेटिंग्स और रिपोर्ट",
            "mr" to "सेटिंग्ज व अहवाल"
        ),
        "present" to mapOf(
            "en" to "Present",
            "hi" to "उपस्थित",
            "mr" to "हजर"
        ),
        "absent" to mapOf(
            "en" to "Absent",
            "hi" to "अनुपस्थित",
            "mr" to "गैरहजर"
        ),
        "half_day" to mapOf(
            "en" to "Half Day",
            "hi" to "आधा दिन",
            "mr" to "अर्धा दिवस"
        ),
        "double_duty" to mapOf(
            "en" to "Double Duty",
            "hi" to "डबल ड्यूटी",
            "mr" to "डबल ड्युटी"
        ),
        "daily_wage" to mapOf(
            "en" to "Daily Wage",
            "hi" to "दैनिक मजदूरी",
            "mr" to "दैनिक मजुरी"
        ),
        "save_attendance" to mapOf(
            "en" to "Save Attendance",
            "hi" to "उपस्थिति सहेजें",
            "mr" to "हजेरी सेव्ह करा"
        ),
        "mark_all_present" to mapOf(
            "en" to "Mark All Present",
            "hi" to "सभी को उपस्थित करें",
            "mr" to "सर्व हजर करा"
        ),
        "clear_all" to mapOf(
            "en" to "Clear All",
            "hi" to "सभी साफ करें",
            "mr" to "सर्व साफ करा"
        ),
        "add_worker" to mapOf(
            "en" to "Add Worker",
            "hi" to "कर्मचारी जोड़ें",
            "mr" to "नवीन कामगार जोडा"
        ),
        "edit_worker" to mapOf(
            "en" to "Edit Worker",
            "hi" to "कर्मचारी संपादित करें",
            "mr" to "कामगार संपादित करा"
        ),
        "worker_name" to mapOf(
            "en" to "Worker Full Name",
            "hi" to "कर्मचारी का पूरा नाम",
            "mr" to "कामगाराचे पूर्ण नाव"
        ),
        "worker_id" to mapOf(
            "en" to "Worker ID Number",
            "hi" to "कर्मचारी आईडी",
            "mr" to "कामगार आयडी"
        ),
        "mobile_number" to mapOf(
            "en" to "Mobile Number",
            "hi" to "मोबाइल नंबर",
            "mr" to "मोबाईल नंबर"
        ),
        "role_category" to mapOf(
            "en" to "Sanitation Role",
            "hi" to "स्वच्छता भूमिका",
            "mr" to "स्वच्छता कामगार पद"
        ),
        "monthly_attendance_pdf" to mapOf(
            "en" to "Monthly Attendance PDF",
            "hi" to "मासिक उपस्थिति पीडीएफ",
            "mr" to "मासिक हजेरी अहवाल (PDF)"
        ),
        "monthly_salary_pdf" to mapOf(
            "en" to "Monthly Salary PDF",
            "hi" to "मासिक वेतन पीडीएफ",
            "mr" to "मासिक पगार अहवाल (PDF)"
        ),
        "share_whatsapp" to mapOf(
            "en" to "Share on WhatsApp",
            "hi" to "व्हाट्सएप पर साझा करें",
            "mr" to "व्हॉट्सअ‍ॅपवर शेअर करा"
        ),
        "share_on_whatsapp" to mapOf(
            "en" to "Share PDF on WhatsApp",
            "hi" to "व्हाट्सएप पर PDF शेयर करें",
            "mr" to "व्हॉट्सअ‍ॅपवर PDF शेअर करा"
        ),
        "share_pdf_on_whatsapp" to mapOf(
            "en" to "Share PDF on WhatsApp",
            "hi" to "व्हाट्सएप पर PDF शेयर करें",
            "mr" to "व्हॉट्सअ‍ॅपवर PDF शेअर करा"
        ),
        "whatsapp_not_installed" to mapOf(
            "en" to "WhatsApp is not installed on this device.",
            "hi" to "इस डिवाइस पर व्हाट्सएप इंस्टॉल नहीं है।",
            "mr" to "या डिव्हाइसवर व्हॉट्सअ‍ॅप इन्स्टॉल नाही."
        ),
        "download_pdf" to mapOf(
            "en" to "Download PDF",
            "hi" to "पीडीएफ डाउनलोड करें",
            "mr" to "PDF डाउनलोड करा"
        ),
        "save_to_device" to mapOf(
            "en" to "Save PDF to Phone",
            "hi" to "फोन में पीडीएफ सहेजें",
            "mr" to "मोबाईलमध्ये सेव्ह करा"
        ),
        "theme" to mapOf(
            "en" to "Theme",
            "hi" to "थीम",
            "mr" to "थीम"
        ),
        "theme_settings" to mapOf(
            "en" to "Theme Settings",
            "hi" to "थीम सेटिंग्स",
            "mr" to "थीम सेटिंग्ज"
        ),
        "theme_settings_desc" to mapOf(
            "en" to "Customize Dark, Light & Custom Appearance",
            "hi" to "डार्क, लाइट और कस्टम रंग अनुकूलित करें",
            "mr" to "डार्क, लाइट आणि सानुकूल रंग निवडा"
        ),
        "dark_theme" to mapOf(
            "en" to "Dark Theme",
            "hi" to "डार्क थीम",
            "mr" to "डार्क थीम"
        ),
        "dark_theme_desc" to mapOf(
            "en" to "Dark background with high contrast text for night & low light use",
            "hi" to "रात और कम रोशनी में उपयोग के लिए गहरा बैकग्राउंड और हल्का टेक्स्ट",
            "mr" to "रात्रीच्या वापरासाठी गडद पार्श्वभूमी व सुस्पष्ट मजकूर"
        ),
        "light_theme" to mapOf(
            "en" to "Light Theme",
            "hi" to "लाइट थीम",
            "mr" to "लाइट थीम"
        ),
        "light_theme_desc" to mapOf(
            "en" to "Clean bright white background with dark text for day use",
            "hi" to "दिन के उपयोग के लिए स्वच्छ सफेद बैकग्राउंड और गहरा टेक्स्ट",
            "mr" to "दिवसाच्या वापरासाठी स्वच्छ पांढरी पार्श्वभूमी व गडद मजकूर"
        ),
        "custom_theme" to mapOf(
            "en" to "Custom Theme",
            "hi" to "कस्टम थीम",
            "mr" to "कस्टम (सानुकूल) थीम"
        ),
        "custom_theme_desc" to mapOf(
            "en" to "Personalize primary, background, button, text and card colors",
            "hi" to "प्राथमिक, बैकग्राउंड, बटन, टेक्स्ट और कार्ड रंग कस्टमाइज़ करें",
            "mr" to "प्राथमिक, पार्श्वभूमी, बटन, मजकूर आणि कार्ड रंग स्वतः निवडा"
        ),
        "primary_color" to mapOf(
            "en" to "Primary Color",
            "hi" to "प्राथमिक रंग (Primary Color)",
            "mr" to "मुख्य रंग (Primary Color)"
        ),
        "background_color" to mapOf(
            "en" to "Background Color",
            "hi" to "बैकग्राउंड रंग (Background)",
            "mr" to "पार्श्वभूमी रंग (Background)"
        ),
        "button_color" to mapOf(
            "en" to "Button Color",
            "hi" to "बटन रंग (Button Color)",
            "mr" to "बटन रंग (Button Color)"
        ),
        "text_color" to mapOf(
            "en" to "Text Color",
            "hi" to "टेक्स्ट रंग (Text Color)",
            "mr" to "मजकूर रंग (Text Color)"
        ),
        "card_color" to mapOf(
            "en" to "Card Color",
            "hi" to "कार्ड रंग (Card Color)",
            "mr" to "कार्ड रंग (Card Color)"
        ),
        "save_theme" to mapOf(
            "en" to "Save Theme",
            "hi" to "थीम सहेजें",
            "mr" to "थीम सेव्ह करा"
        ),
        "live_preview" to mapOf(
            "en" to "Live Appearance Preview",
            "hi" to "लाइव पूर्वावलोकन",
            "mr" to "थेट देखावा पूर्वावलोकन"
        ),
        "reset_theme" to mapOf(
            "en" to "Reset Colors",
            "hi" to "रंग रीसेट करें",
            "mr" to "रंग पूर्ववत करा"
        ),
        "language" to mapOf(
            "en" to "Language",
            "hi" to "भाषा",
            "mr" to "भाषा"
        ),
        "voice_feedback" to mapOf(
            "en" to "Voice Feedback (TTS)",
            "hi" to "आवाज प्रतिक्रिया (TTS)",
            "mr" to "आवाज मार्गदर्शन (TTS)"
        ),
        "total_workers" to mapOf(
            "en" to "Total Workers",
            "hi" to "कुल कर्मचारी",
            "mr" to "एकूण कामगार"
        ),
        "estimated_salary" to mapOf(
            "en" to "Estimated Salary",
            "hi" to "अनुमानित वेतन",
            "mr" to "अंदाजे पगार"
        ),
        "working_days" to mapOf(
            "en" to "Working Days",
            "hi" to "कार्य दिवस",
            "mr" to "कामाचे दिवस"
        ),
        "grand_total" to mapOf(
            "en" to "Grand Total",
            "hi" to "कुल योग",
            "mr" to "एकूण रक्कम"
        ),
        "cloud_backup" to mapOf(
            "en" to "Cloud Backup & Restore",
            "hi" to "क्लाउड बैकअप और पुनर्स्थापना",
            "mr" to "क्लाउड बॅकअप आणि रिस्टोअर"
        ),
        "backup_now" to mapOf(
            "en" to "Backup All Data",
            "hi" to "सभी डेटा का बैकअप लें",
            "mr" to "सर्व डेटा बॅकअप घ्या"
        ),
        "restore_data" to mapOf(
            "en" to "Restore Data",
            "hi" to "डेटा पुनर्स्थापित करें",
            "mr" to "डेटा रिस्टोअर करा"
        ),
        "share_app" to mapOf(
            "en" to "Share Application",
            "hi" to "एप्लिकेशन साझा करें",
            "mr" to "अ‍ॅप शेअर करा"
        ),
        "logout" to mapOf(
            "en" to "Logout Admin",
            "hi" to "लॉगआउट",
            "mr" to "लॉगआउट"
        ),
        "monthly_summary" to mapOf(
            "en" to "Monthly Summary & Logs",
            "hi" to "मासिक सारांश और लॉग",
            "mr" to "मासिक हजेरी सारांश व लॉग"
        ),
        "historical_logs" to mapOf(
            "en" to "Historical Attendance Logs",
            "hi" to "ऐतिहासिक उपस्थिति लॉग",
            "mr" to "मासिक हजेरी इतिहास लॉग"
        ),
        "day_wise_breakdown" to mapOf(
            "en" to "Day-by-Day Logs",
            "hi" to "दिन-वार लॉग",
            "mr" to "दिवसनिहाय हजेरी नोंदी"
        ),
        "worker_wise_summary" to mapOf(
            "en" to "Worker Muster Roll",
            "hi" to "कर्मचारी मस्टर रोल",
            "mr" to "कामगार मस्टर रोल"
        ),
        "select_month" to mapOf(
            "en" to "Select Month",
            "hi" to "महीना चुनें",
            "mr" to "महिना निवडा"
        ),
        "total_days_recorded" to mapOf(
            "en" to "Days Logged",
            "hi" to "दर्ज दिन",
            "mr" to "नोंदवलेले दिवस"
        ),
        "net_man_days" to mapOf(
            "en" to "Net Man-Days",
            "hi" to "कुल मानव-दिवस",
            "mr" to "एकूण मनुष्य-दिवस"
        ),
        "search_worker" to mapOf(
            "en" to "Search worker by name or ID...",
            "hi" to "नाम या आईडी से खोजें...",
            "mr" to "नाव किंवा आयडीने शोधा..."
        ),
        "select_date" to mapOf(
            "en" to "Select Date",
            "hi" to "तारीख चुनें",
            "mr" to "तारीख निवडा"
        ),
        "select_month" to mapOf(
            "en" to "Select Month",
            "hi" to "महीना चुनें",
            "mr" to "महिना निवडा"
        ),
        "delete_confirm" to mapOf(
            "en" to "Are you sure you want to delete this worker?",
            "hi" to "क्या आप वाकई इस कर्मचारी को हटाना चाहते हैं?",
            "mr" to "तुम्हाला नक्की हा कामगार हटवायचा आहे का?"
        ),
        "delete_worker" to mapOf(
            "en" to "Delete Worker",
            "hi" to "कर्मचारी हटाएं",
            "mr" to "कामगार हटवा"
        ),
        "delete_warning_subtext" to mapOf(
            "en" to "This worker will be permanently removed from your active master roster.",
            "hi" to "यह कर्मचारी आपकी सक्रिय सूची से हमेशा के लिए हटा दिया जाएगा।",
            "mr" to "हा कामगार तुमच्या सक्रिय मास्टर यादीतून कायमचा हटवला जाईल."
        ),
        "worker_name_required" to mapOf(
            "en" to "Please enter worker full name",
            "hi" to "कृपया कर्मचारी का पूरा नाम दर्ज करें",
            "mr" to "कृपया कामगाराचे पूर्ण नाव प्रविष्ट करा"
        ),
        "generate_id" to mapOf(
            "en" to "Auto ID",
            "hi" to "ऑटो आईडी",
            "mr" to "ऑटो आयडी"
        ),
        "no_workers_found" to mapOf(
            "en" to "No workers found matching your search",
            "hi" to "आपकी खोज से मेल खाने वाला कोई कर्मचारी नहीं मिला",
            "mr" to "तुमच्या शोधाशी जुळणारा कोणताही कामगार आढळला नाही"
        ),
        "cancel" to mapOf(
            "en" to "Cancel",
            "hi" to "रद्द करें",
            "mr" to "रद्द करा"
        ),
        "delete" to mapOf(
            "en" to "Delete",
            "hi" to "हटाएं",
            "mr" to "हटवा"
        ),
        "save" to mapOf(
            "en" to "Save",
            "hi" to "सहेजें",
            "mr" to "जतन करा"
        ),
        "salary_calculation" to mapOf(
            "en" to "Salary Calculation",
            "hi" to "वेतन गणना",
            "mr" to "पगार गणना (Salary Calc)"
        ),
        "salary_calc_subtitle" to mapOf(
            "en" to "Configurable daily/half-day wage rates, advances, bonus & net paysheets",
            "hi" to "दैनिक/हाफ-डे मजदूरी दर, अग्रिम कटौती, बोनस और शुद्ध वेतन पत्रक",
            "mr" to "दैनिक/हाफ-डे वेतन दर, उचल कपात, बोनस आणि निव्वळ पगार पत्रक"
        ),
        "gross_salary" to mapOf(
            "en" to "Gross Salary",
            "hi" to "सकल वेतन",
            "mr" to "एकूण ढोबळ पगार"
        ),
        "net_salary" to mapOf(
            "en" to "Net Payable Salary",
            "hi" to "अंतिम देय वेतन",
            "mr" to "निव्वळ देय पगार (Net Pay)"
        ),
        "wage_rate_config" to mapOf(
            "en" to "Wage Rate Configuration",
            "hi" to "मजदूरी दर विन्यास",
            "mr" to "वेतन दर नियम सेटिंग"
        ),
        "base_daily_wage" to mapOf(
            "en" to "Base Daily Wage (Full Day)",
            "hi" to "आधार दैनिक मजदूरी (पूरा दिन)",
            "mr" to "मूळ दैनिक वेतन (पूर्ण दिवस)"
        ),
        "half_day_rate" to mapOf(
            "en" to "Half-Day Wage Rate",
            "hi" to "हाफ-डे मजदूरी दर",
            "mr" to "अर्धा दिवस वेतन दर (Half Day)"
        ),
        "double_duty_rate" to mapOf(
            "en" to "Double-Duty Wage Rate",
            "hi" to "डबल-ड्यूटी मजदूरी दर",
            "mr" to "डबल ड्युटी वेतन दर"
        ),
        "rate_mode_multiplier" to mapOf(
            "en" to "Multiplier Factor (Ratio)",
            "hi" to "गुणक अनुपात (Multiplier)",
            "mr" to "गुणांक प्रमाण (उदा. 0.5x, 2.0x)"
        ),
        "rate_mode_fixed" to mapOf(
            "en" to "Fixed Amount (₹ / Half Day)",
            "hi" to "निश्चित राशि (₹ / हाफ डे)",
            "mr" to "निश्चित रक्कम (₹ / हाफ डे)"
        ),
        "role_based_wages" to mapOf(
            "en" to "Role-Based Wage Rates",
            "hi" to "पद आधारित मजदूरी दर",
            "mr" to "पदानुसार वेतन दर"
        ),
        "advances_deductions" to mapOf(
            "en" to "Advance / Loan Deduction",
            "hi" to "अग्रिम / ऋण कटौती",
            "mr" to "उचल / अग्रिम कपात (Advance)"
        ),
        "bonus_incentive" to mapOf(
            "en" to "Bonus / Allowance",
            "hi" to "बोनस / विशेष भत्ता",
            "mr" to "बोनस / विशेष भत्ता (Incentive)"
        ),
        "adjustments" to mapOf(
            "en" to "Salary Adjustments",
            "hi" to "वेतन समायोजन",
            "mr" to "पगार समायोजन (उचल/बोनस)"
        ),
        "payment_status" to mapOf(
            "en" to "Payment Status",
            "hi" to "भुगतान स्थिति",
            "mr" to "पगार वाटप स्थिती"
        ),
        "mark_paid" to mapOf(
            "en" to "Mark as Paid",
            "hi" to "भुगतान किया चिन्हित करें",
            "mr" to "पगार दिला (Paid)"
        ),
        "salary_slip" to mapOf(
            "en" to "Salary Slip",
            "hi" to "वेतन पर्ची",
            "mr" to "पगार पावती (Salary Slip)"
        ),
        "send_salary_slip_whatsapp" to mapOf(
            "en" to "Send Slip via WhatsApp",
            "hi" to "व्हाट्सएप पर पर्ची भेजें",
            "mr" to "व्हॉट्सअ‍ॅपवर पावती पाठवा"
        ),
        "generate_paysheet" to mapOf(
            "en" to "Generate Monthly Paysheet PDF",
            "hi" to "मासिक वेतन पत्रक PDF बनाएं",
            "mr" to "मासिक पगार पत्रक PDF तयार करा"
        ),
        "database_backup" to mapOf(
            "en" to "Database Backup & Export",
            "hi" to "डेटाबेस बैकअप और निर्यात",
            "mr" to "डेटाबेस बॅकअप आणि स्थानिक निर्यात"
        ),
        "export_json_backup" to mapOf(
            "en" to "Export JSON Backup",
            "hi" to "JSON बैकअप निर्यात करें",
            "mr" to "JSON संपूर्ण बॅकअप फाईल"
        ),
        "export_csv_records" to mapOf(
            "en" to "Export CSV Spreadsheet",
            "hi" to "CSV स्प्रेडशीट निर्यात करें",
            "mr" to "CSV एक्सेल फाईल निर्यात करा"
        ),
        "save_to_local_storage" to mapOf(
            "en" to "Save to Local Storage",
            "hi" to "स्थानीय स्टोरेज में सहेजें",
            "mr" to "स्थानिक स्टोरेजमध्ये सेव्ह करा"
        ),
        "restore_database" to mapOf(
            "en" to "Restore Database",
            "hi" to "डेटाबेस पुनर्स्थापित करें",
            "mr" to "डेटाबेस रिस्टोअर (पुनर्प्राप्त) करा"
        ),
        "auto_daily_backup" to mapOf(
            "en" to "Automatic Daily Backup",
            "hi" to "स्वचालित दैनिक बैकअप",
            "mr" to "दररोज स्वयंचलित बॅकअप"
        ),
        "auto_backup_desc" to mapOf(
            "en" to "Automatically backs up database to local storage daily to prevent data loss",
            "hi" to "डेटा हानि रोकने के लिए स्थानीय स्टोरेज में रोजाना स्वचालित बैकअप लेता है",
            "mr" to "डेटा नष्ट होऊ नये म्हणून दररोज स्थानिक स्टोरेजमध्ये स्वयंचलित बॅकअप घेतो"
        ),
        "backup_retention" to mapOf(
            "en" to "Backup Retention Period",
            "hi" to "बैकअप प्रतिधारण अवधि",
            "mr" to "बॅकअप जतन कालावधी"
        ),
        "run_auto_backup_now" to mapOf(
            "en" to "Backup to Storage Now",
            "hi" to "अभी स्टोरेज में बैकअप लें",
            "mr" to "आत्ताच स्टोरेजमध्ये बॅकअप घ्या"
        ),
        "saved_local_backups" to mapOf(
            "en" to "Saved Local Backups",
            "hi" to "सहेजे गए स्थानीय बैकअप",
            "mr" to "जतन केलेले स्थानिक बॅकअप"
        )
    )

    fun get(key: String, langCode: String): String {
        val lang = if (langCode.isBlank()) "mr" else langCode.lowercase()
        val entry = translations[key]
        return entry?.get(lang) ?: entry?.get("en") ?: key
    }

    // Sanitation Worker Roles with multi-lingual label
    val sanitationRoles = listOf(
        "Broom Worker" to mapOf("en" to "Broom Worker", "hi" to "झाड़ू कर्मचारी", "mr" to "झाडू कामगार"),
        "Drain Cleaning Worker" to mapOf("en" to "Drain Cleaning Worker", "hi" to "नाली सफाई कर्मचारी", "mr" to "नाली सफाई कामगार"),
        "Garbage Vehicle Driver" to mapOf("en" to "Garbage Vehicle Driver", "hi" to "कचरा वाहन चालक", "mr" to "कचरा गाडी चालक"),
        "Garbage Vehicle Helper" to mapOf("en" to "Garbage Vehicle Helper", "hi" to "कचरा वाहन सहायक", "mr" to "कचरा गाडी मदतनीस"),
        "Road Sweeper" to mapOf("en" to "Road Sweeper", "hi" to "सड़क सफाई कर्मचारी", "mr" to "रस्ता सफाई कामगार"),
        "Public Toilet Cleaner" to mapOf("en" to "Public Toilet Cleaner", "hi" to "सार्वजनिक शौचालय कर्मचारी", "mr" to "स्वच्छतागृह कामगार"),
        "Supervisor / Mukadam" to mapOf("en" to "Supervisor / Mukadam", "hi" to "पर्यवेक्षक / मुकादम", "mr" to "मुकादम / सुपरवायझर"),
        "Other Sanitation Staff" to mapOf("en" to "Other Sanitation Staff", "hi" to "अन्य स्वच्छता कर्मचारी", "mr" to "इतर स्वच्छता कामगार")
    )

    fun getRoleName(roleKey: String, langCode: String): String {
        val found = sanitationRoles.find { it.first == roleKey }
        if (found != null) {
            val lang = if (langCode.isBlank()) "mr" else langCode.lowercase()
            return found.second[lang] ?: found.second["en"] ?: roleKey
        }
        return roleKey
    }
}
