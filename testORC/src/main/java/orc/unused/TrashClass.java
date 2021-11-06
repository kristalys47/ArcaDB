package orc.unused;

public class TrashClass {

    //        for (int i = 0; i < batch.cols.length; i++) {
//            switch(batch.cols[i].type){
//                case DECIMAL:
//                    bcv[i] = (DecimalColumnVector) batch.cols[i];
//                    break;
//                case DOUBLE:
//                    bcv[i] = (DoubleColumnVector) batch.cols[i];
//                    break;
//                case LONG:
//                    bcv[i] = (LongColumnVector) batch.cols[i];
//                    break;
//                case BYTES:
//                    bcv[i] = (BytesColumnVector) batch.cols[i];
//                    break;
//            }
//        }



    //    public void projection(OrcFilterContext batch) {
//        BytesColumnVector cv = (BytesColumnVector) batch.findColumnVector("val")[0];
//            int index = 0;
//            int[] selected = batch.getSelected();
//
//            for (int i = 0; i < cv.vector.length; i++) {
//                if(cv.vector[i] < 9){ // Condition
//                    selected[index] = i;
//                    index++;
////                }
//            }
//            batch.setSelectedInUse(true);
//            batch.setSelected(selected);
//            batch.setSelectedSize(index);
//    }
//
//    public static void projections(OrcFilterContext gr) {
//        LongColumnVector cv = (LongColumnVector) gr.findColumnVector("val")[0];
//        int index = 0;
//        int[] selected = gr.getSelected();
//        for (int i = 0; i < cv.vector.length; i++) {
//            if (cv.vector[i] < 9) { // Condition
//                selected[index] = i;
//                index++;
//            }
//        }
//        gr.setSelectedInUse(true);
//        gr.setSelected(selected);
//        gr.setSelectedSize(index);
//    }
//
//    Consumer<OrcFilterContext> consumer = gr ->
//    {
//        System.out.printf("m");
//        LongColumnVector cv = (LongColumnVector) gr.findColumnVector("val")[0];
//        int index = 0;
//        int[] selected = gr.getSelected();
//        for (int i = 0; i < cv.vector.length; i++) {
//            if(cv.vector[i] < 9){ // Condition
//                selected[index] = i;
//                index++;
//            }
//        }
//        gr.setSelectedInUse(true);
//        gr.setSelected(selected);
//        gr.setSelectedSize(index);
//    };




    //    public static void readerExplicit(String path, String projections) throws IOException {
//        Configuration conf = new Configuration();
////        OrcConf.READER_USE_SELECTED.setBoolean(conf, true);
//        Reader reader = OrcFile.createReader(new Path(path), OrcFile.readerOptions(conf));
//
//        TypeDescription schema = reader.getSchema();
//        ArrayList<String> names = new  ArrayList<String>(schema.getFieldNames());
//        ArrayList<String> project = new ArrayList<String>(Arrays.asList(projections.split(",")));
//
//        boolean[] finalProjection = new boolean[names.size()+1];
//        finalProjection[0] = true;
//        int countCol = 0;
//        for(int i = 0; i< names.size(); i++) {
//            if(project.contains(names.get(i))){
//                finalProjection[i+1] = true;
//                countCol++;
//            } else{
//                finalProjection[i+1] = false;
//            }
//        }
//
//        Reader.Options readOptions = reader.options();
//        RecordReaderImpl records = (RecordReaderImpl) reader.rows(readOptions);
//        VectorizedRowBatch batch = reader.getSchema().createRowBatchV2();
//
//        int b = 0;
//        int t = 0;
//        while (records.nextBatch(batch)) {
//            TypeDescription td = new TypeDescription(TypeDescription.Category.STRUCT);
//            VectorizedRowBatch vv = new VectorizedRowBatch(20);
//
//            for (int i = 0; i < batch.cols.length; i++) {
//                if (batch.projectedColumns[i] == 1) {
//                    td.addField(names.get(i), new TypeDescription(TypeDescription.Category.LONG));
//                    vv.cols[b] = batch.cols[i];
//                    b++;
//                }
//            }
//            vv.setFilterContext(true, batch.getSelected(), (int) batch.count());
//
//            OrcFile.WriterOptions options = OrcFile.writerOptions(conf).overwrite(true).setSchema(td);
//            Path pathO = new Path("/JavaCode/results" + t);
//            t++;
//            Writer writer = OrcFile.createWriter(pathO, options);
//            writer.addRowBatch(vv);
//            writer.close();
//        }
//        records.close();
//        batch.reset();
//    }


















}
