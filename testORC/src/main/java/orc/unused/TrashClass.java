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
}
