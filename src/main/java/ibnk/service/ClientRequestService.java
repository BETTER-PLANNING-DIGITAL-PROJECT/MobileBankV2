package ibnk.service;

import ibnk.dto.ClientRequestDto;
import ibnk.dto.DashBoardTotalDto;
import ibnk.dto.DataTable;
import ibnk.dto.NotificationEvent;
import ibnk.models.internet.Media;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.client.ClientRequest;
import ibnk.models.internet.enums.EventCode;
import ibnk.models.internet.enums.NotificationChanel;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.SubscriberStatus;
import ibnk.repositories.internet.ClientRequestRepository;
import ibnk.repositories.internet.MediaRepository;
import ibnk.repositories.internet.SubscriptionRepository;
import ibnk.tools.error.ResourceNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ClientRequestService {
    private final ClientRequestRepository clientRequestRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MediaRepository mediaRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    public DataTable listCustomersByType(int pageNumber, int pageSize, String sortDirection, String sortProperty, String propertyValue, String type, String status) {
        ClientRequest exampleRequest = new ClientRequest();

        // Use Java Reflection to set the property dynamically
        try {
            BeanUtils.setProperty(exampleRequest, sortProperty, propertyValue);
            BeanUtils.setProperty(exampleRequest, "customerType", type);
            BeanUtils.setProperty(exampleRequest, "status", status);
        } catch (Exception e) {
            // Handle reflection exception
            return null; // or handle the error accordingly
        }

        // Create ExampleMatcher to match by ignoring case and matching substrings
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.STARTING);

        // Create Example with the dynamically created example user and the matcher
        Example<ClientRequest> example = Example.of(exampleRequest, matcher);

        Pageable pageList = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.valueOf(sortDirection.toUpperCase()), sortProperty));
        Page<ClientRequest> pages = clientRequestRepository.findAll(example, pageList);
        DataTable table = new DataTable();
        List<ClientRequestDto.BasicRequestDto> requests = pages.getContent().stream().map(ClientRequestDto.BasicRequestDto::ModelToDto).toList();
        table.setTotalPages(pages.getTotalPages());
        table.setTotalElements(pages.getTotalElements());
        table.setData(requests);
        return table;
    }

    public DashBoardTotalDto countStatusRequestAndSubStatus() {
        DashBoardTotalDto tlt = new DashBoardTotalDto();
        tlt.setTotalPending(clientRequestRepository.countClientRequestByStatus(Status.PENDING.name()));
        tlt.setTotalRejected(clientRequestRepository.countClientRequestByStatus(Status.REJECTED.name()));
        tlt.setTotalApproved(clientRequestRepository.countClientRequestByStatus(Status.APPROVED.name()));
        tlt.setSubActive(subscriptionRepository.countSubscriptionsByStatus(SubscriberStatus.ACTIVE.name()));
        tlt.setSubInActive(subscriptionRepository.countSubscriptionsByStatus(SubscriberStatus.INACTIVE.name()));
        tlt.setSubPending(subscriptionRepository.countSubscriptionsByStatus(SubscriberStatus.PENDING.name()));
        tlt.setSubBlocked(subscriptionRepository.countSubscriptionsByStatus(SubscriberStatus.BLOCKED.name()));
        tlt.setSubSuspended(subscriptionRepository.countSubscriptionsByStatus(SubscriberStatus.SUSPENDED.name()));
        return tlt;
    }

    private Predicate buildPredicate(Root<ClientRequest> root, CriteriaBuilder cb, String filterProperty, String filterValue) {
        List<Predicate> predicates = new ArrayList<>();

        // Add condition for filtering based on the provided filter property and value
        if (filterProperty != null && filterValue != null) {
            predicates.add(cb.equal(root.get(filterProperty), filterValue));
        }

        // Combine predicates with AND
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    public boolean initiateAccountRequest(ClientRequestDto.BasicRequestDto dto) throws ResourceNotFoundException {

        if (clientRequestRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResourceNotFoundException("email_already_registered");
        }
        ClientRequest cltRequest = ClientRequestDto.BasicRequestDto.DtoToModel(dto);

        cltRequest.setStatus(Status.INITIATED.toString());;
       ClientRequest request =  clientRequestRepository.save(cltRequest);
        dto.setUuid(request.getUuid());
        List<Object> payload = new ArrayList<>();
        payload.add(dto);

        NotificationEvent event = new NotificationEvent();
        event.setEventCode(EventCode.OPEN_ACCOUNT_REQUEST.name());
        event.setPayload(payload);
        event.setType(NotificationChanel.MAIL);
        event.setEmail(dto.getEmail());
        applicationEventPublisher.publishEvent(event);

        return true;
    }

    public String updateAccountRequestIdentification(String uuid, ClientRequestDto.IdentificationDto dto) throws ResourceNotFoundException, ValidationException {
        ClientRequest accountRequest = findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException("no_request_found"));
        if (Objects.equals(dto.getIdentificationType(), "NIC") && dto.getBackIdUuid().isEmpty()) {
            throw new ValidationException("upload_Identification_backImage");
        }
        Optional<Media> clientPhotoMedia = mediaRepository.findByUuid(dto.getPhotoUuid());
        if (clientPhotoMedia.isEmpty()) {
            throw new ValidationException("upload_a_photo");
        }

        Optional<Media> frontIdMedia = mediaRepository.findByUuid(dto.getFrontIdUuid());
        if (frontIdMedia.isEmpty()) {
            throw new ValidationException("upload_front_photo_of_identification");
        }

        Media backIdMedia = new Media();
        if (!dto.getBackIdUuid().isBlank() && !dto.getBackIdUuid().isEmpty()) {
            Optional<Media> media = mediaRepository.findByUuid(dto.getBackIdUuid());
            if (media.isEmpty()) {
                throw new ValidationException("upload_back_photo_of_identification");
            }
            backIdMedia = media.get();
        } else {
            backIdMedia = null;
        }

        accountRequest.setIdentificationType(dto.getIdentificationType());
        accountRequest.setIdentificationNumber(dto.getIdentificationNumber());
        accountRequest.setIdIssueDate(dto.getIdIssueDate());
        accountRequest.setIdExpDate(dto.getIdExpDate());
        accountRequest.setPhoto(clientPhotoMedia.get());
        accountRequest.setFrontIdentification(frontIdMedia.get());
        accountRequest.setBackIdentification(backIdMedia);
        accountRequest.setStatus(Status.PENDING.name());
        accountRequest.setPtcName(dto.getPtcName());
        accountRequest.setPtcAddress(dto.getPtcAddress());
        accountRequest.setPtcPhoneNumber(dto.getPtcPhone());

        clientRequestRepository.save(accountRequest);
        return "Updated";
    }

    public Optional<ClientRequest> findByUuid(String uuid) {
        return clientRequestRepository.findByUuid(uuid);
    }


    public String checkCustomerInfo(String uuid, ClientRequestDto dto, UserEntity user) {
        ClientRequest clientRequest = findByUuid(uuid).orElseThrow();
        clientRequest.setStatus(dto.getStatus());
        clientRequest.setComment(dto.getComment());
        clientRequest.setVerifiedBy(user);
        return switch (Status.valueOf(clientRequest.getStatus())) {
            case APPROVED ->
            //TODO ADD APPROVED PROCEDURE HERE
             "UNDER REVIEW";
            case REJECTED -> {
                List<Object> payload = new ArrayList<>();
                payload.add(clientRequest);

                NotificationEvent event = new NotificationEvent();
                event.setEventCode(EventCode.REJECTED_ACCOUNT_REQUEST.name());
                event.setPayload(payload);
                event.setType(NotificationChanel.MAIL);
                event.setEmail(clientRequest.getEmail());
                applicationEventPublisher.publishEvent(event);
                clientRequestRepository.save(clientRequest);
                yield "event";
            }
            case DOWNLOADED -> {
                clientRequestRepository.save(clientRequest);
                yield "DOWNLOADED";
            }
            default -> "BAD REQUEST";
        };

    }
}
