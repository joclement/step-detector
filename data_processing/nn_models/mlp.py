import tensorflow as tf


def build_mlp_model_fn(dense_layers=(20, 20), learning_rate=0.001, rho=0.95):
    def mlp_model_fn(features, labels, mode):
        """Model function for MLP."""
        acc_feat = features["accelerometer"]
        layer = tf.reshape(acc_feat, [-1, acc_feat.shape[-2] * acc_feat.shape[-1]])

        for units in dense_layers:
            layer = tf.layers.dense(inputs=layer, units=units, activation=tf.nn.relu)
            layer = tf.layers.dropout(inputs=layer, rate=0.2, training=mode == tf.estimator.ModeKeys.TRAIN)

        # Logits Layer
        layer = tf.layers.dense(inputs=layer, units=2)

        predictions = {
            # Generate predictions (for PREDICT and EVAL mode)
            "classes": tf.argmax(input=layer, axis=1),
            # Add `softmax_tensor` to the graph. It is used for PREDICT and by the
            # `logging_hook`.
            "probabilities": tf.nn.softmax(layer, name="softmax_tensor")}

        export_outputs = {'output_prediction': tf.estimator.export.PredictOutput(predictions)}

        if mode == tf.estimator.ModeKeys.PREDICT:
            return tf.estimator.EstimatorSpec(mode=mode, predictions=predictions, export_outputs=export_outputs)

        # Calculate Loss (for both TRAIN and EVAL modes)
        onehot_labels = tf.one_hot(indices=tf.cast(labels, tf.int32), depth=2)
        loss = tf.losses.softmax_cross_entropy(onehot_labels=onehot_labels, logits=layer)

        # Configure the Training Op (for TRAIN mode)
        if mode == tf.estimator.ModeKeys.TRAIN:
            optimizer = tf.train.AdadeltaOptimizer(learning_rate=learning_rate, rho=rho, epsilon=1e-08)
            train_op = optimizer.minimize(loss=loss, global_step=tf.train.get_global_step())
            return tf.estimator.EstimatorSpec(mode=mode, loss=loss, train_op=train_op, export_outputs=export_outputs)

        # Add evaluation metrics (for EVAL mode)
        eval_metric_ops = {"accuracy": tf.metrics.accuracy(labels=labels, predictions=predictions["classes"]),
                           "true_pos": tf.metrics.true_positives(labels=labels, predictions=predictions['classes']),
                           "true_neg": tf.metrics.true_negatives(labels=labels, predictions=predictions['classes']),
                           "false_pos": tf.metrics.false_positives(labels=labels, predictions=predictions['classes']),
                           "false_neg": tf.metrics.false_negatives(labels=labels, predictions=predictions['classes'])}
        return tf.estimator.EstimatorSpec(mode=mode, loss=loss, eval_metric_ops=eval_metric_ops, export_outputs=export_outputs)
    return mlp_model_fn
