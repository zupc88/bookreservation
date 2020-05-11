package bookrental;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="Booklist_table")
public class Booklist {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private String bookid;
        private Long qty;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public String getBookid() {
            return bookid;
        }

        public void setBookid(String bookid) {
            this.bookid = bookid;
        }
        public Long getQty() {
            return qty;
        }

        public void setQty(Long qty) {
            this.qty = qty;
        }

}
