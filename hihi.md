nếu dùng truyền object qua tcp thì phải serialize object (objectoutput stream cũ)
mình cũng không dùng serialization để lưu dữ liệu. dùng cái đấy lúc lưu thì dễ, chỉ cần implement Serializable là
xong, nhưng khi cần tra các auction thì lại phải deserialize hết tất cả auction, item
khi đăng nhập đăng kí thì phải deserialize hết users
thêm sửa xóa thì phải deserialize hết tất cả rồi tìm cái mình muốn. vì dạng dữ liệu khi chưa deserialize thì ko dùng đc
dùng sqlite nó dễ hơn nhiều. nó chỉ là cái bảng, có đầy đủ thông tin, cần cái gì tra cứu rất dễ, cần sửa thì update một cái là xong, cần object thì lấy thông tin ra tái tạo lại object, dễ sắp xếp thứ tự
dùng serialization muốn biết ai bid trước sau, ai max bid cao hơn thì phải deserialize cả object rồi lại sso sánh, đây sqlite chỉ cần só sánh một thuộc tính rồi sắp xếp ngay trên bảng đc. 

mình dùng json để truyền dữ liệu qua tcp - đơn giản hơn, chỉ truyền một số thông tin cần thiết
thuộc tính data khi truyền dữ liệu client <-> server cũ là string -> vấn đề là ví dụ như yêu cầu đặt bid và đăng kí
người dùng khác nhau nhưng lại chỉ gộp chung vào data, các dữ liệu trong data dạng khác nhau, về server rất khó xử lý

do vậy, cần phải phân tách từng loại data của từng loại yêu cầu một. yêu cầu login thì có thuộc tính như nào, yêu cầu 
auction đặt bid lại có thuộc tính khác -> đã thêm protocol là cách đóng gói, network là loại dũ liệu ở trong gói đó

còn về DTO, cái này dùng thay cho domain classes khi đưa dữ liệu client <-> server, dto sẽ loại bỏ thuộc tính như ppassword, không đưa nó đến client 

TODO: các thâyd yêu cầu chạy local nhưng hai máy khác nhau, một client một sẻver thì mình sẽ phải tách server, shared và client ra thành 3 folder lớn riêng, mỗi cái có một pom.xml riêng, máy client có client + shared, máy server có server + shared. 
