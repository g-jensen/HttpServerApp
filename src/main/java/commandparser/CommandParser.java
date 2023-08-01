package commandparser;

import java.util.HashMap;

public class CommandParser {
    public CommandParser() {
        this.intMap = new HashMap<>();
        this.stringMap = new HashMap<>();
        this.usageMap = new HashMap<>();
    }
    public void parseTokens(String[] tokens) throws BadUsageException {
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            try {
                if (intMap.containsKey(token))
                    updateInt(token, tokens[i + 1]);
                if (stringMap.containsKey(token))
                    updateString(token, tokens[i + 1]);
            } catch (Exception e) {
                throw new BadUsageException(token,getUsage(token));
            }
        }
    }
    public void addCommand(String name, Integer defaultValue) {
        intMap.put(name,defaultValue);
    }
    public void addCommand(String name, String defaultValue) {
        stringMap.put(name,defaultValue);
    }
    public void addUsage(String command,String message) {
        this.usageMap.put(command,message);
    }
    public String getUsage(String command) {
        return usageMap.get(command);
    }
    public Integer getInt(String label) {
        return intMap.get(label);
    }
    public String getString(String label) {
        return stringMap.get(label);
    }
    private void updateInt(String label, String token) throws BadUsageException {
        try {
            intMap.put(label,Integer.parseInt(token));
        } catch (Exception e) {
            throw new BadUsageException(label,getUsage(label));
        }
    }
    private void updateString(String label, String token) throws BadUsageException {
        if (commandExists(token))
            throw new BadUsageException(label,getUsage(label));
        stringMap.put(label,token);
    }
    private boolean commandExists(String label) {
        return stringMap.containsKey(label) || intMap.containsKey(label);
    }
    private HashMap<String,String> usageMap;
    private HashMap<String,Integer> intMap;
    private HashMap<String,String> stringMap;
}
