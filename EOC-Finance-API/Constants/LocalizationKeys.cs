namespace EOC.Finance.API.Constants
{
    public static class LocalizationKeys
    {
        // Notification Keys
        public const string MSG_PAYMENT_RECEIVED = "msg_payment_received";
        public const string MSG_EXPENSE_APPROVED = "msg_expense_approved";
        public const string MSG_EXPENSE_REJECTED = "msg_expense_rejected";
        public const string MSG_LOW_FUNDS = "msg_low_funds_warning";
        
        // Audit Action Keys
        public const string ACT_LOGIN_SUCCESS = "act_login_success";
        public const string ACT_LOGIN_FAILED = "act_login_failed";
        public const string ACT_SYNC_COMPLETE = "act_sync_complete";
        
        // Status Keys
        public const string STATUS_PENDING = "status_pending";
        public const string STATUS_APPROVED = "status_approved";
        public const string STATUS_REJECTED = "status_rejected";
    }
}
