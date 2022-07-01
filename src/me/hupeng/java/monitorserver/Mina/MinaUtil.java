package me.hupeng.java.monitorserver.Mina;


import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HUPENG on 2016/9/6.
 */
public class MinaUtil {
    /**
     * �ͻ����ã�
     * ������IP��ַ
     * */
    private String serverAddr = null;

    /**
     * ���
     * */
    private Boolean isServer = null;


    /**
     * һ��serverʵ��
     * */
    private static MinaUtil minaUtilServer = null;

    /**
     * һ��clientʵ��
     * */
    private static MinaUtil minaUtilClient = null;

    /**
     * �ͻ����ã�
     * IoSessionʵ��
     * */
    private IoSession session = null;

    /**
     * һ���򵥵ļ�����������ʵ�ֻص�
     * */
    private SimpleListener simpleListener = null;

    /**
     * һ��bitMapʵ��
     * */


    /**
     * �������ã�
     * NioSocketAcceptor
     * */
    private static NioSocketAcceptor acceptor = null;

    /**
     * �Ự�б�
     * */
    private List<IoSession>sessions = new ArrayList<>();

    class MessageAndIosession{
        public IoSession ioSession;
        public Object message;
    }

    public static MinaUtil getInstance(SimpleListener simpleListener,Boolean isServer,String serverAddr){
        if (isServer){
            if (minaUtilServer == null){
                minaUtilServer = new MinaUtil(simpleListener,isServer,null);
            }
            return minaUtilServer;
        }else {
            if (minaUtilClient == null){
                minaUtilClient = new MinaUtil(simpleListener,isServer,serverAddr);
            }
            return minaUtilClient;
        }


    }



    /**
     * ʵ�ֵ���ģʽ��˽�й��캯��
     * */
    private MinaUtil(SimpleListener simpleListener, Boolean isServer,String serverAddr){
        this.isServer = isServer;
        this.simpleListener = simpleListener;
        if (!isServer) {

            NioSocketConnector connector = new NioSocketConnector();
            connector.setHandler(new MyClientHandler());
            connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MyImageFactory()));
            ConnectFuture future;
            if (serverAddr != null){
                future = connector.connect(new InetSocketAddress(serverAddr, 9191));
            }else {
                future = connector.connect(new InetSocketAddress("192.168.43.1", 9191));
            }

            future.awaitUninterruptibly();
            this.session = future.getSession();
        }else {
            if (acceptor == null){
                acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
            }
            acceptor.setReuseAddress(true);
            acceptor.getSessionConfig().setReadBufferSize(8192);

            acceptor.setHandler(new MyServerHandler());
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MyImageFactory()));

            try {
                acceptor.bind(new InetSocketAddress(9191));
            }catch (Exception e){

            }
        }
    }



    /**
     * ������Ϣ
     * */
    public Boolean send(Object obj){
        if (isServer){
            if (sessions.size() == 0){
                return false;
            }
            Boolean flag = true;
            for(IoSession session: sessions){
                try {
                    session.write(obj);
                }catch (Exception e){

                    flag = false;
                }
            }
            return flag;
        }else {
            try {
                session.write(obj);
            }catch (Exception e){
                return false;
            }
            return true;
        }
    }

    /**
     * ������Ϣ
     * */
    public Boolean send(Object obj,IoSession ioSession){
        try {
            ioSession.write(obj);
        }catch (Exception e){
            return false;
        }
        return true;
    }


    /**
     * handler ����ʵ�ָ��ֿͻ�����Ϣ�Ļص�
     * */
    class MyClientHandler extends IoHandlerAdapter {

        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {
            super.exceptionCaught(session, cause);
            System.out.println(session.getId());
            System.out.println("messageCaught");
            System.out.println(cause.getMessage());
            session.close(false);
        }

        public void messageReceived(IoSession session, Object message)
                throws Exception {

            simpleListener.onReceive(message,session);
            System.out.println(session.getId());
            System.out.println("messageReceived");
        }

        public void messageSent(IoSession session, Object message) throws Exception {
            System.out.println(session.getId());
            System.out.println("messageSent");
        }

        public void sessionClosed(IoSession session) throws Exception {
            System.out.println(session.getId());
            System.out.println("sessionClosed");
        }

        public void sessionCreated(IoSession session) throws Exception {
            System.out.println(session.getId());
            System.out.println("sessionCreated");
        }

        public void sessionIdle(IoSession session, IdleStatus status)
                throws Exception {
            //�ͻ��˿��е�ʱ����лص�
            System.out.println(session.getId());
            System.out.println("sessionIdle");
        }

        public void sessionOpened(IoSession session) throws Exception {
            System.out.println(session.getId());
            System.out.println("sessionOpened");
        }

    }


    /**
     * handler ����ʵ�ָ��ַ�������Ϣ�Ļص�
     * */
    class MyServerHandler extends IoHandlerAdapter {

        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {
            super.exceptionCaught(session, cause);
            System.out.println(session.getId());
            System.out.println("messageCaught");
            System.out.println(cause.getMessage());
        }

        public void messageReceived(IoSession session, Object message)
                throws Exception {
            simpleListener.onReceive(message,session);
        }

        public void messageSent(IoSession session, Object message) throws Exception {
            System.out.println(session.getId());
            System.out.println("messageSent");
        }

        public void sessionClosed(IoSession session) throws Exception {
            System.out.println(session.getId());
            System.out.println("sessionClosed");
            sessions.remove(session);
        }

        public void sessionCreated(IoSession session) throws Exception {
            System.out.println(session.getId());
            System.out.println("sessionCreated");
            sessions.add(session);
        }

        public void sessionIdle(IoSession session, IdleStatus status)
                throws Exception {
            //�ͻ��˿��е�ʱ����лص�
            System.out.println(session.getId());
            System.out.println("sessionIdle");
        }

        public void sessionOpened(IoSession session) throws Exception {
            System.out.println(session.getId());
            System.out.println("sessionOpened");
        }

    }
}
