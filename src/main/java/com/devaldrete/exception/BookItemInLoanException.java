package com.devaldrete.exception;

/**
 * Exception thrown when attempting to delete or modify a book item
 * that is currently involved in an active loan.
 */
public class BookItemInLoanException extends RuntimeException {

  private final Long bookItemId;
  private final String barcode;

  /**
   * Constructs a new exception with the specified book item identifier.
   *
   * @param barcode the barcode of the book item that is currently in a loan
   */
  public BookItemInLoanException(String barcode) {
    super(String.format("Cannot delete book item with barcode '%s' because it is currently borrowed", barcode));
    this.barcode = barcode;
    this.bookItemId = null;
  }

  /**
   * Constructs a new exception with the specified book item ID.
   *
   * @param bookItemId the ID of the book item that is currently in a loan
   */
  public BookItemInLoanException(Long bookItemId) {
    super(String.format("Cannot delete book item with ID '%d' because it is currently borrowed", bookItemId));
    this.bookItemId = bookItemId;
    this.barcode = null;
  }

  /**
   * Constructs a new exception with a custom message.
   *
   * @param barcode the barcode of the book item
   * @param message custom error message
   */
  public BookItemInLoanException(String barcode, String message) {
    super(message);
    this.barcode = barcode;
    this.bookItemId = null;
  }

  public Long getBookItemId() {
    return bookItemId;
  }

  public String getBarcode() {
    return barcode;
  }
}
