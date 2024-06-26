package com.ssafy.farmyo.entity;

import com.ssafy.farmyo.common.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "message")
public class Message extends BaseTime {

    //식별ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    //채팅방매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    //채팅내용
    @Column(name = "chat_content", nullable = false)
    private String content;

    //발신자
    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "buyer_read")
    private int buyerRead = 0;

    @Column(name = "seller_read")
    private int sellerRead = 0;

    //빌더
    @Builder
    public Message(Chat chat, String content, int userId, int buyerRead, int sellerRead) {
        this.chat = chat;
        this.content = content;
        this.userId = userId;
        this.buyerRead = buyerRead;
        this.sellerRead = sellerRead;
    }
}
