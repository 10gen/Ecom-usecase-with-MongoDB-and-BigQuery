view: products {
  sql_table_name: ecommerce.products ;;
  drill_fields: [product_id]

  dimension: product_id {
    primary_key: yes
    type: string
    sql: ${TABLE}.productId ;;
  }

  dimension: _id {
    type: string
    sql: ${TABLE}._id ;;
  }

  dimension: brand {
    type: string
    sql: ${TABLE}.brand ;;
  }

  dimension: category {
    type: string
    sql: ${TABLE}.category ;;
  }

  dimension: price {
    type: string
    sql: ${TABLE}.price ;;
  }

  dimension: product_name {
    type: string
    sql: ${TABLE}.productName ;;
  }

  measure: count {
    type: count
    drill_fields: [product_id, product_name]
  }
}
