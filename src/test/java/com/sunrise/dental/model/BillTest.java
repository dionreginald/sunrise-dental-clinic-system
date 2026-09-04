package com.sunrise.dental.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Bill.calculateTotal().
 * Formula under test: (consultFee + treatmentCost + hospitalCharge) + tax,
 * where tax = subtotal * (taxPercentage / 100).
 * Bill's defaults are hospitalCharge = 500.00 and taxPercentage = 18.0
 * (Sri Lanka VAT rate) — these tests use those real defaults rather than
 * re-deriving the formula, so a change to either constant will correctly
 * break these tests too.
 */
class BillTest {

    private static final double DELTA = 0.001; // tolerance for double comparisons

    @Test
    void calculatesTotalWithConsultFeeAndTreatmentCost() {
        Bill bill = new Bill(null, 1000.00, 2500.00);
        // subtotal = 1000 + 2500 + 500 (hospital charge) = 4000
        // tax = 4000 * 0.18 = 720
        // total = 4720
        double total = bill.calculateTotal();

        assertEquals(4720.00, total, DELTA);
        assertEquals(4720.00, bill.getTotalAmount(), DELTA, "getTotalAmount() should reflect the calculated total");
    }

    @Test
    void calculatesTotalWithDifferentFees() {
        Bill bill = new Bill(null, 500.00, 1000.00);
        // subtotal = 500 + 1000 + 500 = 2000
        // tax = 2000 * 0.18 = 360
        // total = 2360
        double total = bill.calculateTotal();

        assertEquals(2360.00, total, DELTA);
    }

    @Test
    void zeroTreatmentCostStillIncludesConsultFeeAndHospitalCharge() {
        Bill bill = new Bill(null, 750.00, 0.00);
        // subtotal = 750 + 0 + 500 = 1250
        // tax = 1250 * 0.18 = 225
        // total = 1475
        double total = bill.calculateTotal();

        assertEquals(1475.00, total, DELTA);
    }

    @Test
    void customTaxPercentageIsRespected() {
        Bill bill = new Bill(null, 1000.00, 1000.00);
        bill.setTaxPercentage(10.0);   // override the default 18%
        // subtotal = 1000 + 1000 + 500 = 2500
        // tax = 2500 * 0.10 = 250
        // total = 2750
        double total = bill.calculateTotal();

        assertEquals(2750.00, total, DELTA);
    }
}