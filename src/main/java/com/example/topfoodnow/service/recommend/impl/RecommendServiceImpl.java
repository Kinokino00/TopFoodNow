package com.example.topfoodnow.service.recommend.impl;

import com.example.topfoodnow.controller.recommend.dto.Recommend;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.repository.UserRepository;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.repository.RecommendRepository;
import com.example.topfoodnow.service.recommend.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendServiceImpl.class);

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final RecommendRepository recommendRepository;

    // ?°å??¨è–¦
    @Transactional
    public void addRecommend(Recommend Recommend, UserModel currentUserFromSession) {
        UserModel managedUser = userRepository.findById(currentUserFromSession.getId()).orElseThrow(() -> {
            logger.error("?°å??¨è–¦å¤±æ?ï¼šç”¨??ID {} ä¸å??¨æ??ƒè©±å·²é??Ÿã€?, currentUserFromSession.getId());
            return new EntityNotFoundException("?¨æˆ¶ä¸å??¨æ??ƒè©±å·²é??Ÿã€?);
        });
        logger.info("?ºç”¨??ID: {} (Email: {}) ?—è©¦?°å??¨è–¦??, managedUser.getId(), managedUser.getEmail());

        StoreModel store = storeRepository.findByName(Recommend.getStoreName()).orElseGet(() -> {
            StoreModel newStore = new StoreModel();
            newStore.setName(Recommend.getStoreName());
            newStore.setAddress(Recommend.getStoreAddress());
            newStore.setPhotoUrl(Recommend.getStorePhotoUrl());
            logger.info("?°å??°å?å®? {}", newStore.getName());
            return storeRepository.save(newStore);
        });

        if (recommendRepository.findByUserIdAndStoreId(managedUser.getId(), store.getId()).isPresent()) {
            logger.warn("?¨æˆ¶ ID: {} å·²å?åº—å®¶ ID: {} ?‰æ¨?¦ï??¿å??è??°å???, managedUser.getId(), store.getId());
            throw new IllegalArgumentException("?¨å·²ç¶“æ¨?¦é??™å®¶é¤å»³äº†ï?");
        }
        RecommendModel recommend = new RecommendModel();
        recommend.setUserId(managedUser.getId());
        recommend.setStoreId(store.getId());
        recommend.setUser(managedUser);
        recommend.setStore(store);
        recommend.setReason(Recommend.getReason());
        recommend.setScore(Recommend.getScore());
        recommendRepository.save(recommend);
        logger.info("?å??ºç”¨??ID: {} ?°å??¨è–¦ï¼Œå?å®¶ID: {}??, managedUser.getId(), store.getId());
    }

    // ?²å??¨æˆ¶?„æ??‰æ¨??
    @Transactional(readOnly = true)
    public List<Recommend> getRecommendsByUserId(Integer userId) {
        UserModel managedUser = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("?²å??¨è–¦å¤±æ?ï¼šç”¨??ID {} ä¸å??¨ã€?, userId);
            return new EntityNotFoundException("?¨æˆ¶ä¸å??¨ã€?);
        });
        return recommendRepository.findByUserId(managedUser.getId()).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // ç²¾ç¢º?²å??¨æˆ¶å°å?å®¶ç??¨è–¦ï¼Œç”¨?¼æ??‰æ¨?¦è©³?…é???
    @Transactional(readOnly = true)
    public Optional<Recommend> getRecommendByUserAndStoreId(Integer userId, Integer storeId) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("?¥è©¢?¨è–¦å¤±æ?ï¼šç”¨??ID {} ä¸å??¨ã€?, userId);
            return new EntityNotFoundException("?¨æˆ¶ä¸å??¨ã€?);
        });

        Optional<StoreModel> storeOptional = storeRepository.findById(storeId);
        if (storeOptional.isEmpty()) {
            logger.warn("?¥è©¢?¨è–¦å¤±æ?ï¼šå?å®?ID {} ä¸å??¨ã€?, storeId);
            return Optional.empty();
        }
        Optional<RecommendModel> recommendModelOptional = recommendRepository.findByUserIdAndStoreId(user.getId(), storeId);
        return recommendModelOptional.map(this::convertToDto);

    }

    // ?´æ–°?¨è–¦
    @Transactional
    public void updateRecommend(Recommend Recommend, UserModel currentUserFromSession) {
        UserModel managedUser = userRepository.findById(currentUserFromSession.getId()).orElseThrow(() -> {
            logger.error("?´æ–°?¨è–¦å¤±æ?ï¼šç”¨??ID {} ä¸å??¨æ??ƒè©±å·²é??Ÿã€?, currentUserFromSession.getId());
            return new EntityNotFoundException("?¨æˆ¶ä¸å??¨æ??ƒè©±å·²é??Ÿã€?);
        });
        logger.info("?—è©¦?ºç”¨??ID: {} (Email: {}) ?´æ–°?¨è–¦??, managedUser.getId(), managedUser.getEmail());
        StoreModel storeToUpdate = storeRepository.findById(Recommend.getStoreId()).orElseThrow(() -> {
            logger.error("?´æ–°?¨è–¦å¤±æ?ï¼šè??´æ–°?„å?å®¶ä?å­˜åœ¨ï¼Œå?å®¶ID: {}.", Recommend.getStoreId());
            return new EntityNotFoundException("è¦æ›´?°ç?åº—å®¶ä¸å??¨ã€‚å?å®¶ID: " + Recommend.getStoreId());
        });
        RecommendModel existingRecommend = recommendRepository.findByUserIdAndStoreId(managedUser.getId(), storeToUpdate.getId()).orElseThrow(() -> {
            logger.error("?´æ–°?¨è–¦å¤±æ?ï¼šæ‰¾ä¸åˆ°?¨æˆ¶ ID {} å°å?å®?ID {} ?„æ¨?¦æ??¨ç„¡æ¬Šç·¨è¼¯ã€?, managedUser.getId(), Recommend.getStoreId());
            return new EntityNotFoundException("?¾ä??°è©²?¨è–¦?–æ‚¨?¡æ?ç·¨è¼¯?‚ç”¨?¶ID: " + managedUser.getId() + ", åº—å®¶ID: " + Recommend.getStoreId());
        });

        storeToUpdate.setName(Recommend.getStoreName());
        storeToUpdate.setAddress(Recommend.getStoreAddress());
        if (Recommend.getStorePhotoUrl() != null && !Recommend.getStorePhotoUrl().isEmpty()) {
            storeToUpdate.setPhotoUrl(Recommend.getStorePhotoUrl());
        }
        storeRepository.save(storeToUpdate);

        existingRecommend.setReason(Recommend.getReason());
        existingRecommend.setScore(Recommend.getScore());

        recommendRepository.save(existingRecommend);
        logger.info("?å??´æ–°?¨æˆ¶ ID: {} å°å?å®?ID: {} ?„æ¨?¦ã€?, managedUser.getId(), Recommend.getStoreId());
    }

    // ?ªé™¤?¨è–¦
    @Transactional
    public void deleteRecommend(Integer userId, Integer storeId, UserModel currentUserFromSession) {
        UserModel managedUser = userRepository.findById(currentUserFromSession.getId()).orElseThrow(() -> {
            logger.error("?ªé™¤?¨è–¦å¤±æ?ï¼šç”¨??ID {} ä¸å??¨æ??ƒè©±å·²é??Ÿã€?, currentUserFromSession.getId());
            return new EntityNotFoundException("?¨æˆ¶ä¸å??¨æ??ƒè©±å·²é??Ÿã€?);
        });

        RecommendModel recommendToDelete = recommendRepository.findByUserIdAndStoreId(managedUser.getId(), storeId).orElseThrow(() -> {
            logger.error("?ªé™¤?¨è–¦å¤±æ?ï¼šæ‰¾ä¸åˆ°?¨æˆ¶ ID {} å°å?å®?ID {} ?„æ¨?¦æ??¨ç„¡æ¬Šåˆª?¤ã€?, managedUser.getId(), storeId);
            return new EntityNotFoundException("?¾ä??°è©²?¨è–¦?–æ‚¨?¡æ??ªé™¤??);
        });

        recommendRepository.delete(recommendToDelete);
        logger.info("?¨æˆ¶ ID: {} ?ªé™¤äº†å?åº—å®¶ ID: {} ?„æ¨?¦ã€?, managedUser.getId(), storeId);
    }

    // ç¶²ç??€?°æ¨??
    @Transactional(readOnly = true)
    public List<Recommend> findLatestFamousUserRecommends(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return recommendRepository.findLatestFamousUserRecommends(pageable)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // ?€?‰æ¨??
    public Page<Recommend> findAllRecommendsPaged(int page, int size, Boolean isFamousFilter, String searchTerm) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RecommendModel> recommendModelPage = recommendRepository.findFilteredRecommends(isFamousFilter,searchTerm,pageable);
        return recommendModelPage.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<Recommend> findRandom6Recommends() {
        return recommendRepository.findRandom6Recommends().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // å°?RecommendModel è½‰æ???Recommend
    private Recommend convertToDto(RecommendModel recommendModel) {
        Recommend dto = new Recommend();

        if (recommendModel.getUser() != null) {
            dto.setUserId(recommendModel.getUser().getId());
            dto.setUserName(recommendModel.getUser().getUserName());
            dto.setFamous(recommendModel.getUser().getIsFamous());
        } else {
            dto.setUserId(null);
            dto.setUserName("?ªçŸ¥?¨æˆ¶");
            dto.setFamous(false);
        }

        if (recommendModel.getStore() != null) {
            dto.setStoreId(recommendModel.getStore().getId());
            dto.setStoreName(recommendModel.getStore().getName());
            dto.setStoreAddress(recommendModel.getStore().getAddress());
            dto.setStorePhotoUrl(recommendModel.getStore().getPhotoUrl());
        } else {
            dto.setStoreId(null);
            dto.setStoreName("?ªçŸ¥åº—å®¶");
            dto.setStoreAddress("?ªçŸ¥?°å?");
            dto.setStorePhotoUrl("/images/default-image.jpg");
        }
        dto.setReason(recommendModel.getReason());
        dto.setScore(recommendModel.getScore());
        dto.setCreatedAt(recommendModel.getCreatedAt());
        return dto;
    }
}
