package me.hupeng.java.monitorserver.Mina;


import me.hupeng.java.monitorserver.util.OperateImage;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Created by HUPENG on 2016/9/4.
 */
public class MyImageDecoder extends CumulativeProtocolDecoder {

    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        if(ioBuffer.remaining() > 6){//ǰ4�ֽ��ǰ�ͷ
            //��ǵ�ǰposition�Ŀ��ձ��mark���Ա��̵�reset�����ָܻ�positionλ��
            ioBuffer.mark();
//            byte[] l = new byte[4];
//            ioBuffer.get(l);
            //�������ݳ���
            
            int len = ioBuffer.getInt();
            int client_id = ioBuffer.getInt();

            //int len = MyTools.bytes2int(l);//��byteת��int



            if (len == 0){
                ioBuffer.position(ioBuffer.position()+ioBuffer.limit()-4);
                return true;
            }

            //ע�������get�����ᵼ�������remaining()ֵ�����仯
            if(ioBuffer.remaining() < len){
                //�����Ϣ���ݲ����������ûָ�positionλ�õ�����ǰ,������һ��, ���������ݣ���ƴ�ճ���������
                ioBuffer.reset();
                return false;
            }else{
                //��Ϣ�����㹻

                ioBuffer.reset();
                int length = ioBuffer.getInt();
                client_id = ioBuffer.getInt();


                byte dest[] = new byte[length];
                ioBuffer.get(dest);
                
                //ͼƬ�ü�
                dest = OperateImage.cut(dest);
                MyData myData = new MyData();
                myData.bitmap = dest;
                myData.clientId = client_id;
                protocolDecoderOutput.write(myData);
                
                return true;
            }
        }
        return false;//����ɹ����ø�����н����¸���
    }

    

}
