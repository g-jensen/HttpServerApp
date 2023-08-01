package commandparser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    @Test
    void addsCommandsWithInitialValue() {
        CommandParser c = new CommandParser();

        c.addCommand("-p",80);
        assertEquals(80,c.getInt("-p"));

        c.addCommand("-r",".");
        assertEquals(".",c.getString("-r"));
    }

    @Test
    void parsesTokens() throws BadUsageException {
        CommandParser c = new CommandParser();

        c.addCommand("-p",80);
        c.parseTokens(new String[]{"-p","90"});
        assertEquals(90,c.getInt("-p"));

        c.addCommand("-r",".");
        c.parseTokens(new String[]{"-r", "home"});
        assertEquals("home",c.getString("-r"));

        c.parseTokens(new String[]{"-p", "100", "-r", "directory"});
        assertEquals(100,c.getInt("-p"));
        assertEquals("directory",c.getString("-r"));
    }

    @Test
    void throwsBadUsage() {
        CommandParser c = new CommandParser();
        c.addCommand("-p",80);
        c.addCommand("-r",".");

        assertThrows(BadUsageException.class, () ->
                {c.parseTokens(new String[]{"-p"});});
        assertThrows(BadUsageException.class, () ->
                {c.parseTokens(new String[]{"-p", "hi"});});
        assertThrows(BadUsageException.class, () ->
                {c.parseTokens(new String[]{"-p", "-r","home"});});
        assertThrows(BadUsageException.class, () ->
                {c.parseTokens(new String[]{"-r", "-p","90"});});
    }

    @Test
    void addsUsage() {
        CommandParser c = new CommandParser();
        c.addCommand("-p",80);
        c.addUsage("-p","-p <port>");
        c.addCommand("-r",".");
        c.addUsage("-r","-r <directory>");

        assertEquals("-p <port>",c.getUsage("-p"));
        assertEquals("-r <directory>",c.getUsage("-r"));
    }
}