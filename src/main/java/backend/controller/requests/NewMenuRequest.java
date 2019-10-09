package backend.controller.requests;

import backend.model.enums.Category;
import backend.model.enums.OfficeHours;
import org.joda.time.LocalDate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class NewMenuRequest {
    @Positive
    long serviceId;
    @NotEmpty(message = "Please, provide a name to the menu.")
    String name;
    @NotEmpty(message = "Describe what your menu is about ! ! !")
    String description;
    @NotNull(message = "Select a Category for the service.")
    Category category;
    @NotNull(message = "How much does the delivery cost?")
    int deliveryFee;
    @NotNull
    LocalDate startDate;
    @NotNull
    LocalDate endDate;
    @NotNull(message = "Select the delivery hours.")
    OfficeHours deliveryHours;
    @Min(5)
    int averageDeliveryMinutes;
    @NotNull
    int price;
    @NotNull
    int minQuantity;
    @NotNull
    int minQuantityPrice;
    @NotNull
    int maxDailySales;

    public long getServiceId() {
        return serviceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getDeliveryFee() {
        return deliveryFee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public OfficeHours getDeliveryHours() {
        return deliveryHours;
    }

    public int getAverageDeliveryMinutes() {
        return averageDeliveryMinutes;
    }

    public int getPrice() {
        return price;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public int getMinQuantityPrice() {
        return minQuantityPrice;
    }

    public int getMaxDailySales() {
        return maxDailySales;
    }
}
