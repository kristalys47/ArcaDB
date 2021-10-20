#ifndef CTESTS_ORCMANAGEMENT_H
#define CTESTS_ORCMANAGEMENT_H

#include <iostream>
#include <cstdint>

class ORCManagement {
public:
    struct ColumnVectorBatch {
        uint64_t numElements;
        //DataBuffer<char> notNull;
        bool hasNulls;
    };

    void writeORC();
    void readORC();

};
#endif //CTESTS_ORCMANAGEMENT_H
