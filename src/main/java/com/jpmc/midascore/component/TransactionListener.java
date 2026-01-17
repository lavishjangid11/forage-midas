package com.jpmc.midascore.component;

import com.jpmc.midascore.Transaction;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.User;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Autowired
private RestTemplate restTemplate;
private static final String URL =
    "http://localhost:8080/incentive";

@KafkaListener(topics = "${kafka.topic}")
@Transactional
public void listen(Transaction tx) {

    var senderOpt = userRepository.findById(tx.getSenderId());
    var recOpt = userRepository.findById(tx.getRecipientId());

    if (senderOpt.isEmpty() || recOpt.isEmpty())
        return;

    User sender = senderOpt.get();
    User recipient = recOpt.get();

    if (sender.getBalance().compareTo(tx.getAmount()) < 0)
        return;

    // CALL INCENTIVE API
    Incentive incentive =
        restTemplate.postForObject(URL, tx, Incentive.class);

    BigDecimal inc =
        incentive != null ? incentive.getAmount()
                          : BigDecimal.ZERO;

    // BALANCE UPDATE
    sender.setBalance(
        sender.getBalance().subtract(tx.getAmount())
    );

    recipient.setBalance(
        recipient.getBalance()
            .add(tx.getAmount())
            .add(inc)
    );

    TransactionRecord record =
        new TransactionRecord(
            tx.getAmount(),
            sender,
            recipient,
            inc
        );

    recordRepository.save(record);
}


@Component
public class TransactionListener {

    private final UserRepository userRepository;
    private final TransactionRecordRepository recordRepository;

    public TransactionListener(UserRepository userRepository,
                               TransactionRecordRepository recordRepository) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
    }

    @KafkaListener(topics = "${kafka.topic}")
    @Transactional
    public void listen(Transaction tx) {

        var senderOpt = userRepository.findById(tx.getSenderId());
        var recOpt = userRepository.findById(tx.getRecipientId());

        // Validate IDs
        if (senderOpt.isEmpty() || recOpt.isEmpty())
            return;

        User sender = senderOpt.get();
        User recipient = recOpt.get();

        // Validate balance
        if (sender.getBalance().compareTo(tx.getAmount()) < 0)
            return;

        // Update balances
        sender.setBalance(
            sender.getBalance().subtract(tx.getAmount())
        );

        recipient.setBalance(
            recipient.getBalance().add(tx.getAmount())
        );

        // Persist transaction
        TransactionRecord record =
            new TransactionRecord(tx.getAmount(), sender, recipient);

        recordRepository.save(record);
    }
}
