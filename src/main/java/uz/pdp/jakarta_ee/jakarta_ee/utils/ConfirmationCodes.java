package uz.pdp.jakarta_ee.jakarta_ee.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Hashtable;

@Builder
@NoArgsConstructor
public class ConfirmationCodes {
    public static Hashtable<String,Hashtable<String, LocalDateTime>> confirmationCodes = new Hashtable<>();
}
