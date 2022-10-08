package server;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class TelnetTerminal {

    private Path current;
    private ServerSocketChannel server;
    private Selector selector;

    private ByteBuffer buf;

    public TelnetTerminal() throws IOException {
        current = Path.of("common");
        buf = ByteBuffer.allocate(256);
        server = ServerSocketChannel.open();
        selector = Selector.open();
        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        while (server.isOpen()){
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();
            while (keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()){
                    handlerAccept();
                }
                if (key.isReadable()){
                    handlerRead(key);
                }
                keyIterator.remove();
            }
        }

    }

    private void handlerRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        buf.clear();
        StringBuilder sb = new StringBuilder();
        while (true){
            int read = channel.read(buf);
            if (read == 0){
                break;
            }
            if (read == -1){
                channel.close();
                return;
            }
            buf.flip();
            while (buf.hasRemaining()){
                sb.append((char) buf.get());
            }
            buf.clear();
        }
        System.out.println("Received: " + sb);
        String command = sb.toString().trim();
        if (command.equals("ls")){
            String files = Files.list(current).map(path -> path.getFileName().toString())
                    .collect(Collectors.joining("\n\r"));
            channel.write(ByteBuffer.wrap(files.getBytes(StandardCharsets.UTF_8)));
        }else if (command.startsWith("cd")){
            command = command.replaceAll("cd","").trim();
            if (command.equals("..")){
                current = current.getParent();
            }else if (Files.isDirectory(current.resolve(command))){
                current = current.resolve(command);
            } else {
                byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
                channel.write(ByteBuffer.wrap(bytes));
            }
        } else {
            byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
            channel.write(ByteBuffer.wrap(bytes));
        }
        printPath(channel);
    }

    private void printPath(SocketChannel channel) throws IOException {
        String path = current.toString() + " ";
        ByteBuffer response = ByteBuffer.wrap(path.getBytes(StandardCharsets.UTF_8));
        channel.write(response);
    }

    private void handlerAccept() throws IOException {
        SocketChannel socketChannel = server.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted");
    }

    public static void main(String[] args) throws IOException {
        new TelnetTerminal();
    }
}

