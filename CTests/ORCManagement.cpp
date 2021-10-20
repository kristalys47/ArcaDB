#include "ORCManagement.h"

void ORCManagement::writeORC(){
    ORC_UNIQUE_PTR<OutputStream> outStream =writeLocalFile("Test.orc");
    ORC_UNIQUE_PTR<Type> schema(Type::buildTypeFromString("struct<x:int,y:int,name:string,isTest:boolean>"));
    WriterOptions options;
    ORC_UNIQUE_PTR<Writer> writer =createWriter(*schema, outStream.get(), options);

    uint64_t batchSize = 1024, rowCount = 10000;
    ORC_UNIQUE_PTR<ColumnVectorBatch> batch =writer->createRowBatch(batchSize);
    StructVectorBatch *root =dynamic_cast<StructVectorBatch *>(batch.get());
    LongVectorBatch *x =dynamic_cast<LongVectorBatch *>(root->fields[0]);
    LongVectorBatch *y =dynamic_cast<LongVectorBatch *>(root->fields[1]);
    LongVectorBatch *name =dynamic_cast<StringVectorBatch *>(root->fields[2]);
    LongVectorBatch *isTest =dynamic_cast<LongVectorBatch *>(root->fields[3]);

    uint64_t rows = 0;
    for (uint64_t i = 0; i < rowCount; ++i) {
        x->data[rows] = i;
        y->data[rows] = i * 3;
        name->data[rows] = "Hello" + i;
        isTest->data[rows] = i%2 == 1? true: false;
        rows++;

        if (rows == batchSize) {
            root->numElements = rows;
            x->numElements = rows;
            y->numElements = rows;
            name->numElements = rows;
            isTest->numElements = rows;

            writer->add(*batch);
            rows = 0;
        }
    }

    if (rows != 0) {
        root->numElements = rows;
        x->numElements = rows;
        y->numElements = rows;
        name->numElements = rows;
        isTest->numElements = rows;

        writer->add(*batch);
        rows = 0;
    }

    writer->close();
}

void ORCManagement::readORC(){
ORC_UNIQUE_PTR<InputStream> inStream = readLocalFile("Test.orc");
ReaderOptions options;
ORC_UNIQUE_PTR<Reader> reader =createReader(inStream, options);

RowReaderOptions rowReaderOptions;
ORC_UNIQUE_PTR<RowReader> rowReader = reader->createRowReader(rowReaderOptions);
ORC_UNIQUE_PTR<ColumnVectorBatch> batch = rowReader->createRowBatch(1024);

while (rowReader->next(*batch)) {
  for (uint64_t r = 0; r < batch->numElements; ++r) {
        printf(batch[r])
  }
}
}