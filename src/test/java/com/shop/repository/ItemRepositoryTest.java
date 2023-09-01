package com.shop.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @DisplayName("상품 저장 테스트")
    public void createItemTest(){
        //given
        Item item = Item.builder()
                .itemNm("테스트 상품")
                .price(10000)
                .itemDetail("테스트 상품 상세 설명")
                .itemSellStatus(ItemSellStatus.SELL)
                .stockNumber(100)
                .regTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        //when
        Item savedItem = itemRepository.save(item);

        //then
        System.out.println(savedItem);
    }

    public void createItemList(){
        for(int i = 1; i <= 10; i++){
            Item item = Item.builder()
                    .itemNm("테스트 상품" + i)
                    .price(10000 + i)
                    .itemDetail("테스트 상품 상세 설명" + i)
                    .itemSellStatus(ItemSellStatus.SELL)
                    .stockNumber(100)
                    .regTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            itemRepository.save(item);
        }
    }

    @Test
    @DisplayName(("상품명 조회 테스트"))
    public void findByItemNmTest(){
        //given
        this.createItemList();

        //when
        List<Item> itemList = itemRepository.findByItemNm("테스트 상품1");

        //then
        for(Item item : itemList){
            System.out.println(item);
        }
    }

    @Test
    @DisplayName("@Query를 이용한 상품 조회 테스트")
    public void findByItemDetailTest(){
        //given
        this.createItemList();

        //when
        List<Item> itemList = itemRepository.findByItemDetail("테스트 상품 상세 설명");

        //then
        for(Item item : itemList){
            System.out.println(item);
        }
    }

    @Test
    @DisplayName("nativeQuery 속성을 이용한 상품 조회 테스트")
    public void findByItemDetailByNative(){
        //given
        this.createItemList();

        //when
        List<Item> itemList = itemRepository.findByItemDetailByNative("테스트 상품 상세 설명");

        //then
        for(Item item : itemList){
            System.out.println(item);
        }
    }

    @Test
    @DisplayName("QueryDsl 조회 테스트1")
    public void queryDslTest(){
        //given
        this.createItemList();
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QItem qItem = QItem.item;

        //when
        JPAQuery<Item> query = queryFactory.selectFrom(qItem)
                .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL))
                .where(qItem.itemDetail.like("%" + "테스트 상품 상세 설명" + "%"))
                .orderBy(qItem.price.desc());

        List<Item> itemList = query.fetch();
        Long count = query.fetchCount();

        //then
        for(Item item : itemList){
            System.out.println(item);
        }
        System.out.println(count);
    }

    public void createItemList2(){
        for(int i = 1; i <= 5; i++){
            Item item = Item.builder()
                    .itemNm("테스트 상품" + i)
                    .price(10000 + i)
                    .itemDetail("테스트 상품 상세 설명" + i)
                    .itemSellStatus(ItemSellStatus.SELL)
                    .stockNumber(100)
                    .regTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            itemRepository.save(item);
        }

        for(int i = 6; i <= 10; i++){
            Item item = Item.builder()
                    .itemNm("테스트 상품" + i)
                    .price(10000 + i)
                    .itemDetail("테스트 상품 상세 설명" + i)
                    .itemSellStatus(ItemSellStatus.SOLD_OUT)
                    .stockNumber(0)
                    .regTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            itemRepository.save(item);
        }


    }

    @Test
    @DisplayName("QueryDsl 조회 테스트2")
    public void queryDslTest2(){
        //given
        this.createItemList2();

        //when
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QItem item = QItem.item;
        String itemDetail = "테스트 상품 상세 설명";
        int price = 10003;
        String itemSellStat = "SELL";

        booleanBuilder.and(item.itemDetail.like("%" + itemDetail + "%"));
        booleanBuilder.and(item.price.gt(price));

        if(StringUtils.equals(itemSellStat, ItemSellStatus.SELL)){
            booleanBuilder.and(item.itemSellStatus.eq(ItemSellStatus.SELL));
        }

        Pageable pageable = PageRequest.of(0, 5);
        Page<Item> itemPagingResult = itemRepository.findAll(booleanBuilder, pageable);

        //then
        System.out.println("total elements : " + itemPagingResult.getTotalElements());

        List<Item> resultItemList = itemPagingResult.getContent();
        for(Item resultItem : resultItemList){
            System.out.println(resultItem);
        }
    }

}