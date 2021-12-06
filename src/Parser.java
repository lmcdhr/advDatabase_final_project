public class Parser {

    public String[] parse(String query){
        String[] tokens = new String[4];
        String requestType = null;
        int openBracketIdx = query.indexOf("(");
        int startIdx;
        int endIdx;
        if (openBracketIdx != -1){
            requestType = query.substring(0, openBracketIdx);
            //System.out.println("request type is: " + requestType);
        }
        if ("begin".equals(requestType) || "end".equals(requestType) || "beginRO".equals(requestType)) {
            tokens[0] = requestType;
            startIdx = query.indexOf("T") + 1;
            endIdx = query.indexOf(")");
            String transactionID = query.substring(startIdx, endIdx);
            tokens[1] = transactionID;
            //System.out.println("transaction ID is: " + tokens[1]);
        }
        else if ("R".equals(requestType)){
            tokens[0] = requestType;
            startIdx = query.indexOf("T") + 1;
            endIdx = query.indexOf(",");
            String transactionID = query.substring(startIdx, endIdx);
            tokens[1] = transactionID;
            //System.out.println("transaction ID is: " + tokens[1]);

            startIdx = query.indexOf("x") + 1;
            endIdx = query.indexOf(")");
            String dataID = query.substring(startIdx,endIdx);
            tokens[2] = dataID;
            //System.out.println("data ID is: " + tokens[2]);
        }
        else if ("W".equals(requestType)){
            tokens[0] = requestType;
            startIdx = query.indexOf("(") + 1;
            endIdx = query.indexOf(")");
            String subquery = query.substring(startIdx, endIdx);
            String[] temp = subquery.split(",");
            // handle transaction ID and Data ID here
            for (int i=0; i<temp.length - 1; i++){
                tokens[i+1] = temp[i].substring(1);
            }
            tokens[3] = temp[2]; // data value
            //System.out.println("transaction ID is: " + tokens[1]);
            //System.out.println("data ID is: " + tokens[2]);
            //System.out.println("data value is: " + tokens[3]);
        }
        else if ("fail".equals(requestType) || "recover".equals(requestType)){
            tokens[0] = requestType;
            startIdx = query.indexOf("(") + 1;
            endIdx = query.indexOf(")");
            String siteID = query.substring(startIdx, endIdx);
            tokens[1] = siteID;
            //System.out.println("site ID is: " + tokens[1]);
        }
        else if ("dump".equals(requestType)){
            tokens[0] = requestType;
            //System.out.println("" + tokens[0]);
        }


        return tokens;
    }
}
