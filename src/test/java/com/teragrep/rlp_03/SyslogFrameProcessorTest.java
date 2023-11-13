package com.teragrep.rlp_03;

import com.teragrep.rlp_01.RelpCommand;
import com.teragrep.rlp_01.RelpFrameTX;
import com.teragrep.rlp_03.context.RelpFrameServerRX;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class SyslogFrameProcessorTest {

    private RelpFrameServerRX createOpenFrame () {
        String requestData = "relp_version=0\n"
                + "relp_software=RLP-01,1.0.1,https://teragrep.com\n"
                + "commands=" + RelpCommand.SYSLOG;

        int requestDataLength =
                requestData.getBytes(StandardCharsets.UTF_8).length;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(requestDataLength);

        byteBuffer.put(requestData.getBytes(StandardCharsets.UTF_8));
        byteBuffer.flip(); // syslog frameprocessor assumes buffers in read mode

        return new RelpFrameServerRX(0, RelpCommand.OPEN,
                requestDataLength, byteBuffer, null);
    }

    private RelpFrameServerRX createSyslogFrame(String requestData) {
        int requestDataLength =
                requestData.getBytes(StandardCharsets.UTF_8).length;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(requestDataLength);

        byteBuffer.put(requestData.getBytes(StandardCharsets.UTF_8));
        byteBuffer.flip(); // syslog frameprocessor assumes buffers in read mode

        return new RelpFrameServerRX(0, RelpCommand.SYSLOG,
                requestDataLength, byteBuffer, null);
    }

    private RelpFrameServerRX createCloseFrame() {
        String requestData = "";
        int requestDataLength =
                requestData.getBytes(StandardCharsets.UTF_8).length;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(requestDataLength);

        byteBuffer.put(requestData.getBytes(StandardCharsets.UTF_8));
        byteBuffer.flip(); // syslog frameprocessor assumes buffers in read mode

        return new RelpFrameServerRX(0, RelpCommand.CLOSE,
                requestDataLength, byteBuffer, null);
    }

    @Test
    public void testOpen() {
        List<byte[]> messageList = new LinkedList<>();

        Consumer<byte[]> testConsumer = messageList::add;

        SyslogFrameProcessor frameProcessor = new SyslogFrameProcessor(testConsumer);

        RelpFrameTX txFrame = frameProcessor.process(createOpenFrame());

        Assertions.assertEquals(RelpCommand.RESPONSE, txFrame.getCommand());
        Assertions.assertEquals(0, messageList.size());
    }

    @Test
    public void testSyslogCommand() {
        List<byte[]> messageList = new LinkedList<>();

        Consumer<byte[]> testConsumer = messageList::add;

        SyslogFrameProcessor frameProcessor = new SyslogFrameProcessor(testConsumer);


        RelpFrameTX txFrame = frameProcessor.process(createSyslogFrame("test message"));

        Assertions.assertEquals(RelpCommand.RESPONSE, txFrame.getCommand());

        Assertions.assertEquals(
                "test message",
                new String(messageList.get(0), StandardCharsets.UTF_8)
        );
    }

    @Test
    public void testOpenSyslogCloseSequence() {
        List<byte[]> messageList = new LinkedList<>();

        Consumer<byte[]> testConsumer = messageList::add;

        SyslogFrameProcessor frameProcessor = new SyslogFrameProcessor(testConsumer);

        RelpFrameTX txFrameOpenResponse = frameProcessor.process(createOpenFrame());
        Assertions.assertEquals(RelpCommand.RESPONSE, txFrameOpenResponse.getCommand());

        RelpFrameTX txFrameSyslogResponse = frameProcessor.process(createSyslogFrame("test message"));
        Assertions.assertEquals(RelpCommand.RESPONSE, txFrameSyslogResponse.getCommand());

        RelpFrameTX txFrameCloseResponse = frameProcessor.process(createCloseFrame());
        /* FIXME close
        Assertions.assertEquals(RelpCommand.RESPONSE, txFrameCloseResponse.getCommand());

        RelpFrameTX txFrameServerCloseResponse = txDeque.removeFirst();

         */
        Assertions.assertEquals(RelpCommand.SERVER_CLOSE,
                txFrameCloseResponse.getCommand());


        Assertions.assertEquals(
                "test message",
                new String(messageList.get(0), StandardCharsets.UTF_8)
        );
    }
}
