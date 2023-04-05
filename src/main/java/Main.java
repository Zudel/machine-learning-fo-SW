
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String PR_NAME ="Avro";
    private static final String LINK ="https://github.com/Zudel/avro";
    public String getIDJira(String shortestMessage){
        String s=null;
        Pattern pattern= Pattern.compile(PR_NAME.toUpperCase()+"-\\d+");
        Matcher matcher= pattern.matcher(shortestMessage);
        if(matcher.find()){
            s=matcher.group(0);
        }
        return s;
    }


}
