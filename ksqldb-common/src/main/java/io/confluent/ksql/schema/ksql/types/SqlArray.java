/*
 * Copyright 2019 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.schema.ksql.types;

import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.Immutable;
import io.confluent.ksql.schema.ksql.DataException;
import io.confluent.ksql.schema.ksql.FormatOptions;
import io.confluent.ksql.schema.ksql.SchemaConverters;
import io.confluent.ksql.schema.ksql.SqlBaseType;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Immutable
public final class SqlArray extends SqlType {

  private final SqlType itemType;

  public static SqlArray of(final SqlType itemType) {
    return new SqlArray(itemType);
  }

  private SqlArray(final SqlType itemType) {
    super(SqlBaseType.ARRAY);
    this.itemType = requireNonNull(itemType, "itemType");
  }

  public SqlType getItemType() {
    return itemType;
  }

  @Override
  public void validateValue(final Object value) {
    if (value == null) {
      return;
    }

    if (!(value instanceof List)) {
      final SqlBaseType sqlBaseType = SchemaConverters.javaToSqlConverter()
          .toSqlType(value.getClass());

      throw new DataException("Expected ARRAY, got " + sqlBaseType);
    }

    final List<?> array = (List<?>) value;

    IntStream.range(0, array.size()).forEach(idx -> {
      try {
        final Object element = array.get(idx);
        itemType.validateValue(element);
      } catch (final DataException e) {
        throw new DataException("ARRAY element " + (idx + 1) + ": " + e.getMessage(), e);
      }
    });
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SqlArray array = (SqlArray) o;
    return Objects.equals(itemType, array.itemType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemType);
  }

  @Override
  public String toString() {
    return toString(FormatOptions.none());
  }

  @Override
  public String toString(final FormatOptions formatOptions) {
    return "ARRAY<" + itemType.toString(formatOptions) + '>';
  }
}
