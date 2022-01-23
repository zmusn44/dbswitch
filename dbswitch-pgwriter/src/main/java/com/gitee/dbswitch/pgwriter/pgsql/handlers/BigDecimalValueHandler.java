package com.gitee.dbswitch.pgwriter.pgsql.handlers;

import com.gitee.dbswitch.pgwriter.util.BigDecimalUtils;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The Algorithm for turning a BigDecimal into a Postgres Numeric is heavily inspired by the
 * Intermine Implementation:
 * <p>
 * https://github.com/intermine/intermine/blob/master/intermine/objectstore/main/src/org/intermine/sql/writebatch/BatchWriterPostgresCopyImpl.java
 */
public class BigDecimalValueHandler<T extends Number> extends BaseValueHandler<T> {

  private static final int DECIMAL_DIGITS = 4;
  private static final BigInteger TEN_THOUSAND = new BigInteger("10000");

  @Override
  protected void internalHandle(final DataOutputStream buffer, final T value) throws IOException {
    final BigDecimal tmpValue = getNumericAsBigDecimal(value);

    // Number of fractional digits:
    final int fractionDigits = tmpValue.scale();

    // Number of Fraction Groups:
    final int fractionGroups = (fractionDigits + 3) / 4;

    final List<Integer> digits = digits(tmpValue);

    buffer.writeInt(8 + (2 * digits.size()));
    buffer.writeShort(digits.size());
    buffer.writeShort(digits.size() - fractionGroups - 1);
    buffer.writeShort(tmpValue.signum() == 1 ? 0x0000 : 0x4000);
    buffer.writeShort(fractionDigits);

    // Now write each digit:
    for (int pos = digits.size() - 1; pos >= 0; pos--) {
      final int valueToWrite = digits.get(pos);
      buffer.writeShort(valueToWrite);
    }
  }

  @Override
  public int getLength(final T value) {
    final List<Integer> digits = digits(getNumericAsBigDecimal(value));
    return (8 + (2 * digits.size()));
  }

  private static BigDecimal getNumericAsBigDecimal(final Number source) {
    if (!(source instanceof BigDecimal)) {
      return BigDecimalUtils.toBigDecimal(source.doubleValue());
    }

    return (BigDecimal) source;
  }

  private List<Integer> digits(final BigDecimal value) {
    BigInteger unscaledValue = value.unscaledValue();

    if (value.signum() == -1) {
      unscaledValue = unscaledValue.negate();
    }

    final List<Integer> digits = new ArrayList<>();

    // The scale needs to be a multiple of 4:
    int scaleRemainder = value.scale() % 4;

    // Scale the first value:
    if (scaleRemainder != 0) {
      final BigInteger[] result = unscaledValue
          .divideAndRemainder(BigInteger.TEN.pow(scaleRemainder));
      final int digit = result[1].intValue() * (int) Math.pow(10, DECIMAL_DIGITS - scaleRemainder);
      digits.add(digit);
      unscaledValue = result[0];
    }

    while (!unscaledValue.equals(BigInteger.ZERO)) {
      final BigInteger[] result = unscaledValue.divideAndRemainder(TEN_THOUSAND);
      digits.add(result[1].intValue());
      unscaledValue = result[0];
    }

    return digits;
  }
}
