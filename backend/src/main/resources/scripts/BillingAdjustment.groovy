import java.math.BigDecimal

class BillingAdjustment {
    BigDecimal adjust(BigDecimal amount) {
        if (!amount) {
            return BigDecimal.ZERO
        }
        return amount + new BigDecimal("12.34")
    }
}
