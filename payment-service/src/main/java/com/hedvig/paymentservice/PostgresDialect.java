package com.hedvig.paymentservice;

import java.sql.Types;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class PostgresDialect extends PostgreSQL94Dialect {

  public PostgresDialect() {
    super();
    this.registerColumnType(Types.BLOB, "BYTEA");
  }

  @Override
  public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor descriptor) {
    if (descriptor.getSqlType() == Types.BLOB) {
      return BinaryTypeDescriptor.INSTANCE;
    }

    return super.remapSqlTypeDescriptor(descriptor);
  }
}
