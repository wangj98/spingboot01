package com.wang.springboot.service.Imp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.springboot.dao.MessageDao;
import com.wang.springboot.demain.Message;
import com.wang.springboot.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MessageServiceImp implements MessageService {
    @Autowired
    private MessageDao messageDao;

    //记录一条记录
    @Override
    public boolean insert(Integer fromUid, Integer toUid, String text) {
        ChatIdServiceImp chatIdServiceImp = new ChatIdServiceImp();
        //通过两个人的uid他们的cid,如果是第一次会话则新建保存他们的cid
        Integer cid = chatIdServiceImp.select(fromUid, toUid).getCid();
        if (cid == null) {
            chatIdServiceImp.save(fromUid, toUid);

        }
        Message message = new Message();
        message.setCid(cid);
        message.setFromUid(fromUid);
        message.setToUid(toUid);
        message.setText(text);
        return messageDao.insert(message) > 0;
    }


    //将两人的记录都查找出来
    @Override
    public List<Message> selectAll(Integer uid_a, Integer uid_b) {
        QueryWrapper<Message> qw = new QueryWrapper<>();
        QueryWrapper<Message> qw1 = new QueryWrapper<>();

        qw.eq("fromUid", uid_a).eq("toUid", uid_b);
        qw1.eq("fromUid", uid_b).eq("toUid", uid_a);
        List<Message> messages = messageDao.selectList(qw);
        messages.addAll(messageDao.selectList(qw1));
        return messages;


    }

    //查询一个人发送给另外一人的信息
    @Override
    public List<Message> selectFromTo(Integer fromUid, Integer toUid) {
        QueryWrapper<Message> qw = new QueryWrapper<>();
        qw.eq("fromUid", fromUid).eq("toUid", toUid);
        return messageDao.selectList(qw);
    }


    //将每次的数据保存到数据库
    @Override
    public boolean messageInsert(Message message) {

        return messageDao.insert(message) != 0;//插入成功时,返回真
    }

    //按页查询信息:先查到两人对应的会话,根据会话查询聊天记录
    @Override
    public Page<Message> messageIPage(Integer user_a,Integer user_b,Integer page, Integer num) {
        Page<Message> objectPage = new Page<>();
        //查询cid
        QueryWrapper<Message> qw = new QueryWrapper<>();
        qw.eq("formUid", user_a).eq("toUid",user_b);
        Integer cid = messageDao.selectOne(qw).getCid();
        QueryWrapper<Message> qw1 = new QueryWrapper<>();
        //根据cid查找倒序数据
        qw1.eq("cid",cid).orderByDesc("id");

        return messageDao.selectPage(objectPage, qw1);
    }
}
