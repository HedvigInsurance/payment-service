package com.hedvig.paymentservice.services.payments.reporting;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Map;

public final class MonthlyTransactionsAggregations {
  private final Map<Year, BigDecimal> student;
  private final Map<Year, BigDecimal> household;
  private final Map<Year, BigDecimal> house;
  private final Map<Year, BigDecimal> total;

    @java.beans.ConstructorProperties({"student", "household", "house", "total"})
    public MonthlyTransactionsAggregations(Map<Year, BigDecimal> student, Map<Year, BigDecimal> household, Map<Year, BigDecimal> house, Map<Year, BigDecimal> total) {
        this.student = student;
        this.household = household;
        this.house = house;
        this.total = total;
    }

    public Map<Year, BigDecimal> getStudent() {
        return this.student;
    }

    public Map<Year, BigDecimal> getHousehold() {
        return this.household;
    }

    public Map<Year, BigDecimal> getHouse() {
        return this.house;
    }

    public Map<Year, BigDecimal> getTotal() {
        return this.total;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MonthlyTransactionsAggregations))
            return false;
        final MonthlyTransactionsAggregations other = (MonthlyTransactionsAggregations) o;
        final Object this$student = this.getStudent();
        final Object other$student = other.getStudent();
        if (this$student == null ? other$student != null : !this$student.equals(other$student)) return false;
        final Object this$household = this.getHousehold();
        final Object other$household = other.getHousehold();
        if (this$household == null ? other$household != null : !this$household.equals(other$household)) return false;
        final Object this$house = this.getHouse();
        final Object other$house = other.getHouse();
        if (this$house == null ? other$house != null : !this$house.equals(other$house)) return false;
        final Object this$total = this.getTotal();
        final Object other$total = other.getTotal();
        if (this$total == null ? other$total != null : !this$total.equals(other$total)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $student = this.getStudent();
        result = result * PRIME + ($student == null ? 43 : $student.hashCode());
        final Object $household = this.getHousehold();
        result = result * PRIME + ($household == null ? 43 : $household.hashCode());
        final Object $house = this.getHouse();
        result = result * PRIME + ($house == null ? 43 : $house.hashCode());
        final Object $total = this.getTotal();
        result = result * PRIME + ($total == null ? 43 : $total.hashCode());
        return result;
    }

    public String toString() {
        return "MonthlyTransactionsAggregations(student=" + this.getStudent() + ", household=" + this.getHousehold() + ", house=" + this.getHouse() + ", total=" + this.getTotal() + ")";
    }
}
