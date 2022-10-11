view: orders_line_items {
  sql_table_name: ecommerce.orders_lineItems ;;

  dimension: _id {
    type: string
    sql: ${TABLE}._id ;;
  }

  dimension: line_items_brand {
    type: string
    sql: ${TABLE}.`lineItems.brand` ;;
  }

  dimension: line_items_category {
    type: string
    sql: ${TABLE}.`lineItems.category` ;;
  }

  dimension: line_items_idx {
    type: number
    value_format_name: id
    sql: ${TABLE}.lineItems_idx ;;
  }

  dimension: line_items_price {
    type: string
    sql: ${TABLE}.`lineItems.price` ;;
  }

  dimension: line_items_product_id {
    type: string
    sql: ${TABLE}.`lineItems.productId` ;;
  }

  dimension: line_items_product_name {
    type: string
    sql: ${TABLE}.`lineItems.productName` ;;
  }

  dimension: line_items_quantity {
    type: string
    sql: ${TABLE}.`lineItems.quantity` ;;
  }

  measure: count {
    type: count
    drill_fields: [line_items_product_name]
  }
}
