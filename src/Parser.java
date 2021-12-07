public class Parser {

    public String[] parse(String query){
        String[] tokens = new String[4];
        String requestType = null;
        int openBracketIdx = query.indexOf("(");
        int startIdx;
        int endIdx;
        if (openBracketIdx != -1){
            requestType = query.substring(0, openBracketIdx);
        }
        if ("begin".equals(requestType) || "end".equals(requestType) || "beginRO".equals(requestType)) {
            tokens[0] = requestType;
            startIdx = query.indexOf("T") + 1;
            endIdx = query.indexOf(")");
            String transactionID = query.substring(startIdx, endIdx).trim();
            tokens[1] = transactionID;
        }
        else if ("R".equals(requestType)){
            tokens[0] = requestType;
            startIdx = query.indexOf("T") + 1;
            endIdx = query.indexOf(",");
            String transactionID = query.substring(startIdx, endIdx).trim();
            tokens[1] = transactionID;

            startIdx = query.indexOf("x") + 1;
            endIdx = query.indexOf(")");
            String dataID = query.substring(startIdx,endIdx).trim();
            tokens[2] = dataID;
        }
        else if ("W".equals(requestType)){
            tokens[0] = requestType;
            startIdx = query.indexOf("(") + 1;
            endIdx = query.indexOf(")");
            String subquery = query.substring(startIdx, endIdx);
            String[] temp = subquery.split(",");
            // handle transaction ID and Data ID here
            for (int i=0; i<temp.length - 1; i++){
                tokens[i+1] = temp[i].trim().substring(1);
            }
            tokens[3] = temp[2].trim(); // data value
        }
        else if ("fail".equals(requestType) || "recover".equals(requestType)){
            tokens[0] = requestType;
            startIdx = query.indexOf("(") + 1;
            endIdx = query.indexOf(")");
            String siteID = query.substring(startIdx, endIdx).trim();
            tokens[1] = siteID;
        }
        else if ("dump".equals(requestType)){
            tokens[0] = "dump";
        }


        return tokens;
    }
}
