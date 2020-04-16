package com.inspur.bss.waf.common.typehandler;

import com.inspur.bss.waf.common.annotation.EnumPersistenceValue;
import com.inspur.bss.waf.manage.instance.enums.ProdVersionType;
import com.inspur.bss.waf.manage.wafconfig.enums.CcMatchRuleType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @Author hexinyu
 * 2019/09/22
 */
public class MyEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private Class<E> type;
    private final E[] enums;
    private EnumPersistenceValue persistenceValue;
    private Field field;

    public MyEnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
        this.enums = type.getEnumConstants();
        Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.getAnnotation(EnumPersistenceValue.class) != null)
                .findFirst().ifPresent( field -> {
                    this.persistenceValue = field.getAnnotation(EnumPersistenceValue.class);
                    this.field = field;
                    this.field.setAccessible(true);
        });
        if (this.enums == null && !type.equals(Enum.class)) {
            throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        if(field == null){
            ps.setInt(i, parameter.ordinal());
        }else{
            ps.setObject(i, ReflectionUtils.getField(this.field,parameter));
        }

    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if(field == null) {
            int i = rs.getInt(columnName);
            if (rs.wasNull()) {
                return null;
            } else {
                try {
                    return enums[i];
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Cannot convert " + i + " to " + type.getSimpleName() + " by ordinal value.", ex);
                }
            }
        }else {
            Object object = rs.getObject(columnName, field.getType());
            return Arrays.stream(type.getEnumConstants()).filter( e -> ReflectionUtils.getField(field,e).equals(object)).findFirst().orElse(null);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if( field == null ) {
            int i = rs.getInt(columnIndex);
            if (rs.wasNull()) {
                return null;
            } else {
                try {
                    return enums[i];
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Cannot convert " + i + " to " + type.getSimpleName() + " by ordinal value.", ex);
                }
            }
        }else {
            Object object = rs.getObject(columnIndex, field.getType());
            return Arrays.stream(type.getEnumConstants()).filter( e -> Objects.equals(ReflectionUtils.getField(field, e), object)).findFirst().orElse(null);
        }
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if( field == null ) {
            int i = cs.getInt(columnIndex);
            if (cs.wasNull()) {
                return null;
            } else {
                try {
                    return enums[i];
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Cannot convert " + i + " to " + type.getSimpleName() + " by ordinal value.", ex);
                }
            }
        }
        else {
            Object object = cs.getObject(columnIndex, field.getType());
            return Arrays.stream(type.getEnumConstants()).filter( e -> Objects.equals(ReflectionUtils.getField(field, e), object)).findFirst().orElse(null);
        }
    }
}
