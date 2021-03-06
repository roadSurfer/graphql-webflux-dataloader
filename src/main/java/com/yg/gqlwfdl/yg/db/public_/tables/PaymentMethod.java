/*
 * This file is generated by jOOQ.
 */
package com.yg.gqlwfdl.yg.db.public_.tables;


import com.yg.gqlwfdl.yg.db.public_.Indexes;
import com.yg.gqlwfdl.yg.db.public_.Keys;
import com.yg.gqlwfdl.yg.db.public_.Public;
import com.yg.gqlwfdl.yg.db.public_.tables.records.PaymentMethodRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PaymentMethod extends TableImpl<PaymentMethodRecord> {

    private static final long serialVersionUID = 333406130;

    /**
     * The reference instance of <code>PUBLIC.PAYMENT_METHOD</code>
     */
    public static final PaymentMethod PAYMENT_METHOD = new PaymentMethod();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PaymentMethodRecord> getRecordType() {
        return PaymentMethodRecord.class;
    }

    /**
     * The column <code>PUBLIC.PAYMENT_METHOD.ID</code>.
     */
    public final TableField<PaymentMethodRecord, Long> ID = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>PUBLIC.PAYMENT_METHOD.DESCRIPTION</code>.
     */
    public final TableField<PaymentMethodRecord, String> DESCRIPTION = createField("DESCRIPTION", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>PUBLIC.PAYMENT_METHOD.CHARGE</code>.
     */
    public final TableField<PaymentMethodRecord, Double> CHARGE = createField("CHARGE", org.jooq.impl.SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * Create a <code>PUBLIC.PAYMENT_METHOD</code> table reference
     */
    public PaymentMethod() {
        this(DSL.name("PAYMENT_METHOD"), null);
    }

    /**
     * Create an aliased <code>PUBLIC.PAYMENT_METHOD</code> table reference
     */
    public PaymentMethod(String alias) {
        this(DSL.name(alias), PAYMENT_METHOD);
    }

    /**
     * Create an aliased <code>PUBLIC.PAYMENT_METHOD</code> table reference
     */
    public PaymentMethod(Name alias) {
        this(alias, PAYMENT_METHOD);
    }

    private PaymentMethod(Name alias, Table<PaymentMethodRecord> aliased) {
        this(alias, aliased, null);
    }

    private PaymentMethod(Name alias, Table<PaymentMethodRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> PaymentMethod(Table<O> child, ForeignKey<O, PaymentMethodRecord> key) {
        super(child, key, PAYMENT_METHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PRIMARY_KEY_D);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<PaymentMethodRecord, Long> getIdentity() {
        return Keys.IDENTITY_PAYMENT_METHOD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PaymentMethodRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_D;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PaymentMethodRecord>> getKeys() {
        return Arrays.<UniqueKey<PaymentMethodRecord>>asList(Keys.CONSTRAINT_D);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentMethod as(String alias) {
        return new PaymentMethod(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentMethod as(Name alias) {
        return new PaymentMethod(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public PaymentMethod rename(String name) {
        return new PaymentMethod(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public PaymentMethod rename(Name name) {
        return new PaymentMethod(name, null);
    }
}
