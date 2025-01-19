package com.springboot.member.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.entity.Stamp;
import com.springboot.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member createMember(Member member) {
        // 등록된 이메일인지 확인
        verifyExistsEmail(member.getEmail());
        // 멤버 엔티티의 유효성 검증이 끝나면 등록과 동시에 스탬프 생성
        member.setStamp(new Stamp());
        // 스탬프까지 보유한 완전한 엔티티 등록 후 반환
        return memberRepository.save(member);
    }

    public Member updateMember(Member member) {
        // 파라미터로 받는 member는 memberUpdateDto를 엔티티로 매핑해놓은 것이기 때문에 null이면 원래 필드의 데이터, 값이 있으면 그 값으로 수정해야함
        // 아이디는 가지고 있기 때문에 아이디로 해당하는 member 소환
        Member foundMember = findVerifiedMember(member.getMemberId());
        // 필드별 값 있으면 수정하는데 변경 가능한 필드만 확인
        Optional.ofNullable(member.getPhone())
                .ifPresent(phone -> foundMember.setPhone(phone));
        Optional.ofNullable(member.getName())
                .ifPresent(foundMember::setName);
        Optional.ofNullable(member.getMemberStatus())
                .ifPresent(foundMember::setMemberStatus);
        // 수정했으면 ModifiedAt도 업데이트 해줘야하지만 BaseEntity의 Listner에서 자동으로 감시하고 수정해줌
        // 변경된 값을 DB에 다시 저장
        return memberRepository.save(foundMember);

    }

    public Member findMember(long memberId) {
        // 전달받은 memberId로 찾아서 반환
        return findVerifiedMember(memberId);
    }

    public Page<Member> findMembers(int page, int size) {
        // 전달받은 page와 size로 PageRequest객체 생성해서 넣고 반환
        return memberRepository.findAll(PageRequest.of(page-1, size, Sort.by("memberId").descending()));
    }

    public void deleteMember(long memberId) {
        // 삭제를 한다고 DB에서 삭제를 하면 안되기 때문에 멤버의 상태만 탈퇴 상태로 변경
        Member foundMember = findVerifiedMember(memberId);
        foundMember.setMemberStatus(Member.MemberStatus.MEMBER_QUIT);
        memberRepository.save(foundMember);
    }

    public Member findVerifiedMember(long memberId) {
        // memberId를 받아서 repository에서 찾은 후 있는지 검증하고 없으면 Exception 발생
        Optional<Member> optionalMember =
                memberRepository.findById(memberId);
        Member findMember =
                optionalMember.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        return findMember;
    }

    // 가입 시 존재하는 이미 존재하는 이메일인지 확인해야함
    private void verifyExistsEmail(String email) {
        // Repository에 findByEmail 메서드 추상화 시켜놓고 사용
        // 있으면 Exception 발생시켜야함
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent())
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
    }
}
