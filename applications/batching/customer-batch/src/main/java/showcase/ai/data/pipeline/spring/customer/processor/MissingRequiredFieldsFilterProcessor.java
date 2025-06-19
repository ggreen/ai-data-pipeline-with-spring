package showcase.ai.data.pipeline.spring.customer.processor;

import lombok.RequiredArgsConstructor;
import nyla.solutions.core.io.csv.CsvWriter;
import nyla.solutions.core.util.Text;
import org.springframework.batch.item.ItemProcessor;
import showcase.ai.data.pipeline.spring.customer.domain.Customer;

@RequiredArgsConstructor
public class MissingRequiredFieldsFilterProcessor implements ItemProcessor<Customer,Customer> {

    private final CsvWriter csvWriter;

    /**
     *
     * @param customer to be processed, never {@code null}.
     * @return Null if customer does not have all required fields, else return customer
     * @throws Exception
     */
    @Override
    public Customer process(Customer customer) throws Exception {
        if(customer == null )
            return null;

        if(
                customer.id() == null || customer.id().isBlank() ||
                customer.firstName() == null || customer.firstName().isBlank() ||
                customer.lastName() == null || customer.lastName().isBlank() ||
                customer.contact() == null ||
                        customer.contact().phone() == null || customer.contact().phone().isBlank() ||
                        customer.contact().email() == null || customer.contact().email().isBlank())
        {
            csvWriter.appendRow(
                    customer.id(),
                    customer.firstName(),
                    customer.lastName(),
                    Text.toString(customer.contact()),
                    Text.toString(customer.location()));
            return null;
        }

        return customer;
    }
}
