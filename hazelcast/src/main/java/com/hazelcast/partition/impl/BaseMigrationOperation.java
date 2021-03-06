/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.partition.impl;

import com.hazelcast.core.MemberLeftException;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.partition.MigrationCycleOperation;
import com.hazelcast.partition.MigrationInfo;
import com.hazelcast.spi.AbstractOperation;
import com.hazelcast.spi.ExceptionAction;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public abstract class BaseMigrationOperation extends AbstractOperation
        implements MigrationCycleOperation, PartitionAwareOperation {

    protected MigrationInfo migrationInfo;
    protected boolean success;

    public BaseMigrationOperation() {
    }

    public BaseMigrationOperation(MigrationInfo migrationInfo) {
        this.migrationInfo = migrationInfo;
        setPartitionId(migrationInfo.getPartitionId());
    }

    public MigrationInfo getMigrationInfo() {
        return migrationInfo;
    }

    @Override
    public Object getResponse() {
        return success;
    }

    @Override
    public final boolean validatesTarget() {
        return false;
    }

    @Override
    public ExceptionAction onInvocationException(Throwable throwable) {
        if (throwable instanceof MemberLeftException) {
            return ExceptionAction.THROW_EXCEPTION;
        }
        if (!migrationInfo.isValid()) {
            return ExceptionAction.THROW_EXCEPTION;
        }
        return super.onInvocationException(throwable);
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        migrationInfo.writeData(out);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        migrationInfo = new MigrationInfo();
        migrationInfo.readData(in);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{partitionId=" + getPartitionId() + ", migration=" + migrationInfo + '}';
    }
}
