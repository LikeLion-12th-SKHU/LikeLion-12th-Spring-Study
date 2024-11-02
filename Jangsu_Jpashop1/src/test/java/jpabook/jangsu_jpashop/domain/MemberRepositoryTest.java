package jpabook.jangsu_jpashop.domain;

import jakarta.transaction.Transactional;
import jpabook.jangsu_jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class MemberRepositoryTest {
    MemberRepository memberRepository;

    @Test
    @Rollback(value = false)
    @Transactional
    public void testMember() {
        Member member = new Member();
        member.setName("memberA");
        Long savedId = memberRepository.save(member);

        Member findMember = memberRepository.findOne(savedId);

        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getName()).isEqualTo(member.getName());
        Assertions.assertThat(findMember).isEqualTo(member);
    }

}
