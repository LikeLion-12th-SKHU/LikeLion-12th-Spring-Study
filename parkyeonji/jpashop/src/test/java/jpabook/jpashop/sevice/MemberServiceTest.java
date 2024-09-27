package jpabook.jpashop.sevice;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Test
    public void 회원가입() throws Exception {
        //given (이런게 주어졌을 때)
        Member member = new Member();
        member.setName("kim");

        //when (이렇게 하면)
        Long savedId = memberService.join(member);

        //then (이렇게 된다)
        assertEquals(member, memberRepository.findOne(savedId)); //true면 정상적으로 이루어졌다고 보면 됨 //같은 트랜잭션 안에서 같은 id인 경우 여러 개로 생기지 않고 하나로 관리가 되기 때문에 가능
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예약() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        memberService.join(member2); // 예외가 발생해야 함


        //then
        fail("예외가 발생해야 합니다.");
    }

}