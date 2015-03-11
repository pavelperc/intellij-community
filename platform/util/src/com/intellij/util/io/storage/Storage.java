/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

/*
 * @author max
 */
package com.intellij.util.io.storage;

import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.util.io.PagePool;

import java.io.File;
import java.io.IOException;

public class Storage extends AbstractStorage {
  public Storage(String path) throws IOException {
    super(path);
  }

  public Storage(String path, PagePool pool) throws IOException {
    super(path, pool);
  }

  public Storage(String path, CapacityAllocationPolicy capacityAllocationPolicy) throws IOException {
    super(path, capacityAllocationPolicy);
  }

  @Override
  protected AbstractRecordsTable createRecordsTable(PagePool pool, File recordsFile) throws IOException {
    return new RecordsTable(recordsFile, pool);
  }

  public int createNewRecord() throws IOException {
    synchronized (myLock) {
      return myRecordsTable.createNewRecord();
    }
  }

  public void replaceBytes(int record, int offset, ByteSequence bytes) throws IOException {
    synchronized (myLock) {
      final int changedBytesLength = bytes.getLength();

      final int currentSize = myRecordsTable.getSize(record);
      assert currentSize >= 0;
      assert offset + bytes.getLength() <= currentSize;

      if (changedBytesLength == 0) return;

      final long address = myRecordsTable.getAddress(record);

      myDataTable.writeBytes(address + offset, bytes.getBytes(), bytes.getOffset(), bytes.getLength());
    }
  }

  public void deleteRecord(int record) throws IOException {
    assert record > 0;
    synchronized (myLock) {
      doDeleteRecord(record);
    }
  }
}
